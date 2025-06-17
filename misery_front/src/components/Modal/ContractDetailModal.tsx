// src/components/Modal/ContractDetailModal.tsx
import React, { useState, useEffect } from 'react';
import Modal from 'react-modal';
import moment from 'moment';

import * as S from './ContractDetailModal.styles'; // 변경된 스타일 파일
import { ModalHeader, ModalLogo, LogoCircle, CloseButton, ModalFooter, FooterButton } from './styles'; // 공용 모달 스타일
import CustomModal from './Modal'; 

import { 
  getContractDetails, 
  signContract, 
  getCurrentUser, 
  downloadContractFileDirectly,
  verifyContractIntegrity, 
  getContractPreviewBlob  // 새로운 함수 사용
} from '../../utils/api';

// 타입 정의
interface ContractDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  contractId: number | null;
  onContractUpdate?: () => void;
}

interface UserSignature {
  signerUuid: string;
  signerUsername: string;
  signedAt: string;
  signatureHash: string;
}

interface Version {
  id: number;
  versionNumber: number;
  filePath: string;
  fileHash: string;
  status: 'PENDING_SIGNATURE' | 'SIGNED' | 'ARCHIVED';
  createdAt: string;
  storageProvider: string;
  bucketName: string;
  signatures: UserSignature[];
}

interface Participant {
  userUuid: string;
  username: string;
  email: string;
  role: 'INITIATOR' | 'COUNTERPARTY';
}

interface ContractUser {
  id: number;
  username: string;
  email: string;
}

interface ContractDetail {
  id: number;
  title: string;
  description?: string;
  status: 'OPEN' | 'CLOSED' | 'CANCELLED';
  createdAt: string;
  updatedAt?: string;
  createdBy: ContractUser;
  updatedBy?: ContractUser;
  currentVersion?: Version;
  participants: Participant[];
  versionHistory: Version[];
}

// currentUser 상태를 위한 타입
interface CurrentUser extends ContractUser {
  uuid?: string; // API 응답에 따라 uuid가 있을 수 있음
  userUuid?: string;
}

// verificationResult 상태를 위한 타입
interface VerificationStep {
  status: string;
  details: string;
  discrepancies: string[];
}
interface VerificationResultData {
  overallSuccess: boolean;
  message: string;
  verifiedAt: string;
  dbVerification: VerificationStep;
  blockchainVerification: VerificationStep;
  // contractVersionId: number; // 필요시 추가
}


const ContractDetailModal: React.FC<ContractDetailModalProps> = ({
  isOpen,
  onClose,
  contractId,
  onContractUpdate,
}) => {
  const [contract, setContract] = useState<ContractDetail | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [signing, setSigning] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [currentUser, setCurrentUser] = useState<CurrentUser | null>(null);
  const [pdfUrl, setPdfUrl] = useState<string | null>(null);
  const [pdfLoading, setPdfLoading] = useState<boolean>(false);
  const [verifying, setVerifying] = useState<boolean>(false);
  const [verificationResult, setVerificationResult] = useState<VerificationResultData | null>(null);
  const [showVerificationResult, setShowVerificationResult] = useState<boolean>(false);

  useEffect(() => {
    const loadCurrentUser = async () => {
      try {
        const response = await getCurrentUser();
        if (response.success) {
          setCurrentUser(response.data);
        }
      } catch (err) {
        console.error('현재 사용자 정보 로드 실패:', err);
      }
    };

    if (isOpen) {
      loadCurrentUser();
    } else {
      setContract(null);
      setError(null);
      setCurrentUser(null);
      setVerificationResult(null);
      setShowVerificationResult(false);
      if (pdfUrl) {
        URL.revokeObjectURL(pdfUrl);
        setPdfUrl(null);
      }
    }
  }, [isOpen, pdfUrl]);

  useEffect(() => {
    if (isOpen && contractId) {
      loadContractDetails(contractId);
    }
  }, [isOpen, contractId]);

  // PDF URL 생성
  useEffect(() => {
    if (contract?.currentVersion?.filePath && isOpen && !pdfUrl) {
      generatePdfBlobUrl(contract.currentVersion.filePath);
    }
  }, [contract, isOpen, pdfUrl]);

  const loadContractDetails = async (currentContractId: number) => {
    if (!currentContractId) return;
    
    setLoading(true);
    setError(null);
    
    try {
      const response = await getContractDetails(currentContractId);
      if (response.success) {
        setContract(response.data);
      } else {
        setError(response.message || '계약서 정보를 불러올 수 없습니다.');
      }
    } catch (err) {
      setError('계약서 정보를 불러오는 중 오류가 발생했습니다.');
      console.error('계약서 상세 로드 오류:', err);
    } finally {
      setLoading(false);
    }
  };

  const generatePdfBlobUrl = async (filePath: string) => {
    try {
      setPdfLoading(true);
      const blob = await getContractPreviewBlob(filePath);
      const url = URL.createObjectURL(blob);
      setPdfUrl(url);
    } catch (err) {
      console.error('PDF 로드 오류:', err);
      setError('PDF 파일을 불러오는 중 오류가 발생했습니다.');
    } finally {
      setPdfLoading(false);
    }
  };

  const downloadPdf = async () => { 
    if (!contract?.currentVersion?.filePath) return;
    try {
      const fileName = `${contract.title}_v${contract.currentVersion.versionNumber}.pdf`;
      await downloadContractFileDirectly(contract.currentVersion.filePath, fileName);
    } catch (err) {
      console.error('다운로드 오류:', err);
      alert('파일 다운로드 중 오류가 발생했습니다.');
    }
  };
  
  const handleVerifyIntegrity = async () => {
    if (!contract || !contract.currentVersion) {
      alert('현재 버전 정보가 없습니다.');
      return;
    }
    setVerifying(true);
    try {
      const response = await verifyContractIntegrity(contract.id, contract.currentVersion.versionNumber);
      if (response.success) {
        setVerificationResult(response.data);
        setShowVerificationResult(true);
      } else {
        alert(response.message || '무결성 검증에 실패했습니다.');
      }
    } catch (err) {
      console.error('무결성 검증 오류:', err);
      alert('무결성 검증 중 오류가 발생했습니다.');
    } finally {
      setVerifying(false);
    }
  };

  const handleSign = async () => {
    if (!contractId || !contract) return;
    const confirmSign = window.confirm(
      `"${contract.title}" 계약서에 서명하시겠습니까?\n\n서명 후에는 취소할 수 없습니다.`
    );
    if (!confirmSign) return;
    setSigning(true);
    try {
      const response = await signContract(contractId);
      if (response.success) {
        alert('계약서에 성공적으로 서명했습니다.');
        await loadContractDetails(contractId); 
        if (onContractUpdate) {
          onContractUpdate();
        }
      } else {
        alert(response.message || '서명에 실패했습니다.');
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '서명 중 오류가 발생했습니다.';
      alert(errorMessage);
      console.error('서명 오류:', err);
    } finally {
      setSigning(false);
    }
  };

  const handleClose = () => {
    setPdfUrl(null); // 단순히 null로 설정
    onClose();
  };

  const hasUserSigned = () => {
    if (!contract || !contract.currentVersion || !currentUser) {
      return false;
    }
    const userUuid = currentUser.uuid || currentUser.userUuid || currentUser.id?.toString();
    return contract.currentVersion.signatures.some(signature => 
        signature.signerUuid === userUuid || 
        signature.signerUuid === currentUser.id?.toString() ||
        (currentUser.uuid && signature.signerUuid === currentUser.uuid) ||
        (currentUser.userUuid && signature.signerUuid === currentUser.userUuid)
    );
  };

   const isUserParticipant = () => {
    if (!contract || !currentUser) {
      return false;
    }
    const isCreator = contract.createdBy.id === currentUser.id;
    const userUuid = currentUser.uuid || currentUser.userUuid || currentUser.id?.toString();
    const isParticipantInList = contract.participants.some(participant => 
        participant.userUuid === userUuid ||
        participant.userUuid === currentUser.id?.toString() ||
        (currentUser.uuid && participant.userUuid === currentUser.uuid) ||
        (currentUser.userUuid && participant.userUuid === currentUser.userUuid) ||
        participant.email === currentUser.email
    );
    return isCreator || isParticipantInList;
  };

  const canSign = () => {
    if (!contract || !contract.currentVersion || !currentUser) return false;
    if (contract.status !== 'OPEN') return false;
    if (contract.currentVersion.status !== 'PENDING_SIGNATURE') return false;
    if (hasUserSigned()) return false;
    if (!isUserParticipant()) return false;
    return true;
  };

  const getCurrentUserSignature = () => {
    if (!contract || !contract.currentVersion || !currentUser) return null;
    const userUuid = currentUser.uuid || currentUser.userUuid || currentUser.id?.toString();
    if (!userUuid) return null;
    return contract.currentVersion.signatures.find(signature => 
        signature.signerUuid === userUuid ||
        signature.signerUuid === currentUser.id?.toString() ||
        (currentUser.uuid && signature.signerUuid === currentUser.uuid) ||
        (currentUser.userUuid && signature.signerUuid === currentUser.userUuid)
    );
  };

  const getSigningStatusDisplay = () => {
    if (!contract || !currentUser) return null;
    if (!isUserParticipant()) {
      return { type: 'info', title: 'ℹ️ 참여자 아님', message: '이 계약서의 참여자가 아닙니다.', backgroundColor: '#fff3e0', borderColor: '#ff9800', textColor: '#f57c00' };
    }
    if (hasUserSigned()) {
      const userSignature = getCurrentUserSignature();
      return { type: 'success', title: '✅ 서명 완료', message: userSignature ? `${moment(userSignature.signedAt).format('YYYY년 MM월 DD일 HH:mm')}에 서명을 완료했습니다.` : '이미 서명을 완료했습니다.', backgroundColor: '#e8f5e8', borderColor: '#4caf50', textColor: '#2e7d32' };
    }
    if (contract.status === 'CLOSED') {
      return { type: 'info', title: 'ℹ️ 계약 완료', message: '이미 완료된 계약서입니다.', backgroundColor: '#e3f2fd', borderColor: '#2196f3', textColor: '#1976d2' };
    }
    if (contract.status === 'CANCELLED') {
      return { type: 'warning', title: '⚠️ 계약 취소', message: '취소된 계약서입니다.', backgroundColor: '#ffebee', borderColor: '#f44336', textColor: '#d32f2f' };
    }
    if (contract.currentVersion?.status === 'SIGNED') {
      return { type: 'info', title: 'ℹ️ 서명 완료', message: '모든 서명이 완료된 계약서입니다.', backgroundColor: '#e8f5e8', borderColor: '#4caf50', textColor: '#2e7d32' };
    }
    if (contract.currentVersion?.status === 'ARCHIVED') {
      return { type: 'info', title: 'ℹ️ 보관됨', message: '보관된 버전입니다.', backgroundColor: '#f5f5f5', borderColor: '#9e9e9e', textColor: '#666' };
    }
    if (canSign()) {
      return { type: 'canSign', title: '✓ 서명 가능', message: '이 계약서에 서명할 수 있습니다.', backgroundColor: '#e8f5e8', borderColor: '#4caf50', textColor: '#2e7d32' };
    }
    return null;
  };
  
  const statusDisplay = getSigningStatusDisplay();

  const getStatusText = (status: string) => {
    switch (status) {
      case 'OPEN': return '진행중';
      case 'CLOSED': return '완료';
      case 'CANCELLED': return '취소됨';
      case 'PENDING_SIGNATURE': return '서명 대기';
      case 'SIGNED': return '서명 완료';
      case 'ARCHIVED': return '보관됨';
      default: return status;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'OPEN': case 'PENDING_SIGNATURE': return '#1976d2';
      case 'CLOSED': case 'SIGNED': return '#388e3c';
      case 'CANCELLED': return '#d32f2f';
      case 'ARCHIVED': return '#666';
      default: return '#666';
    }
  };

  const getRoleText = (role: string) => {
    switch (role) {
      case 'INITIATOR': return '계약자';
      case 'COUNTERPARTY': return '피계약자';
      default: return role;
    }
  };
  
  const getVerificationStatusText = (vStatus: string) => {
    switch (vStatus) {
      case 'SUCCESS': return '✅ 성공';
      case 'FAILED': return '❌ 실패';
      case 'DATA_NOT_FOUND': return '⚠️ 데이터 없음';
      case 'ERROR': return '🔴 오류';
      case 'NOT_CHECKED': return '⏸️ 검사 안함';
      default: return vStatus;
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onRequestClose={handleClose}
      style={{
        ...CustomModal,
        content: { 
          ...CustomModal.content,
          width: '90%', 
          maxWidth: '1200px', 
          height: '80vh', 
          padding: '0', 
        },
      }}
      contentLabel="계약서 상세"
    >
      <ModalHeader>
        <ModalLogo>
          <LogoCircle>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
              <path d="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z" />
            </svg>
          </LogoCircle>
        </ModalLogo>
        <CloseButton type="button" onClick={handleClose}>
          <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
            <path d="M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z" />
          </svg>
        </CloseButton>
      </ModalHeader>

      <S.ModalBodyWrapper>
        <S.PdfPreviewContainer>
          {pdfLoading && <S.PdfMessage>PDF 미리보기 로딩 중...</S.PdfMessage>}
          {!pdfLoading && pdfUrl && (
            <S.PdfFrame 
              src={pdfUrl} 
              title={`${contract?.title || '계약서'} 미리보기`}
            />
          )}
          {!pdfLoading && !pdfUrl && contract?.currentVersion?.filePath && !error && (
            <S.PdfMessage>PDF를 불러오고 있습니다...</S.PdfMessage>
          )}
          {!pdfLoading && !pdfUrl && !contract?.currentVersion?.filePath && !error && (
             <S.PdfMessage>표시할 PDF 파일이 없습니다.</S.PdfMessage>
          )}
          {error && !pdfLoading && <S.PdfMessage style={{color: 'red'}}>{error}</S.PdfMessage>}
        </S.PdfPreviewContainer>

        <S.DetailsContainer>
          {loading && !contract && <S.PdfMessage>계약서 정보를 불러오는 중...</S.PdfMessage>}
          
          {contract && (
            <>
              <S.InfoSectionTitle style={{ fontSize: '1.5rem', marginBottom: '1rem' }}>{contract.title}</S.InfoSectionTitle>
              
              <S.ActionButtonGroup>
                {contract.currentVersion && (
                    <>
                        <S.ActionButton variant="success" onClick={downloadPdf}>
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M5,20H19V18H5M19,9H15V3H9V9H5L12,16L19,9Z" /></svg>
                            PDF 다운로드
                        </S.ActionButton>
                        <S.ActionButton variant="warning" onClick={handleVerifyIntegrity} disabled={verifying}>
                            {verifying ? '검증 중...' : (
                                <>
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M23,12L20.56,9.22L20.9,5.54L17.29,4.72L15.4,1.54L12,3L8.6,1.54L6.71,4.72L3.1,5.53L3.44,9.21L1,12L3.44,14.78L3.1,18.47L6.71,19.29L8.6,22.47L12,21L15.4,22.46L17.29,19.28L20.9,18.46L20.56,14.78L23,12M10,17L6,13L7.41,11.59L10,14.17L16.59,7.58L18,9L10,17Z"/></svg>
                                무결성 검증
                                </>
                            )}
                        </S.ActionButton>
                    </>
                )}
              </S.ActionButtonGroup>

              <S.InfoSection>
                <S.InfoItem>
                  <strong>계약 상태:</strong> 
                  <S.StatusBadge statusColor={getStatusColor(contract.status)}>
                    {getStatusText(contract.status)}
                  </S.StatusBadge>
                </S.InfoItem>
                {contract.description && (
                  <S.InfoItem><strong>설명:</strong> {contract.description}</S.InfoItem>
                )}
                <S.InfoItem><strong>생성일:</strong> {moment(contract.createdAt).format('YYYY년 MM월 DD일 HH:mm')}</S.InfoItem>
                <S.InfoItem><strong>생성자:</strong> {contract.createdBy.username} ({contract.createdBy.email})</S.InfoItem>
                {contract.updatedAt && contract.updatedBy && (
                    <S.InfoItem><strong>최종 수정:</strong> {moment(contract.updatedAt).format('YYYY년 MM월 DD일 HH:mm')} by {contract.updatedBy.username}</S.InfoItem>
                )}
              </S.InfoSection>

              {contract.currentVersion && (
                <S.InfoSection>
                  <S.InfoSectionTitle>
                     <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20M11,13V15H13V13H11M11,16V18H13V16H11M7,13V15H9V13H7M7,16V18H9V16H7Z" /></svg>
                    현재 버전 (v{contract.currentVersion.versionNumber})
                  </S.InfoSectionTitle>
                  <S.InfoItem>
                    <strong>버전 상태:</strong> 
                    <S.StatusBadge statusColor={getStatusColor(contract.currentVersion.status)}>
                        {getStatusText(contract.currentVersion.status)}
                    </S.StatusBadge>
                  </S.InfoItem>
                  <S.InfoItem><strong>업로드일:</strong> {moment(contract.currentVersion.createdAt).format('YYYY년 MM월 DD일 HH:mm')}</S.InfoItem>
                  <S.InfoItem>
                    <strong>파일 해시:</strong> 
                    <S.FileHashText>{contract.currentVersion.fileHash.substring(0, 20)}...</S.FileHashText>
                  </S.InfoItem>
                </S.InfoSection>
              )}

              {contract.participants.length > 0 && (
                <S.InfoSection>
                  <S.InfoSectionTitle>
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M12,5.5A3.5,3.5 0 0,1 15.5,9A3.5,3.5 0 0,1 12,12.5A3.5,3.5 0 0,1 8.5,9A3.5,3.5 0 0,1 12,5.5M5,8C5.56,8 6.08,8.15 6.53,8.42C6.38,9.85 6.8,11.27 7.66,12.38C7.16,13.34 6.16,14 5,14A3,3 0 0,1 2,11A3,3 0 0,1 5,8M19,8A3,3 0 0,1 22,11A3,3 0 0,1 19,14C17.84,14 16.84,13.34 16.34,12.38C17.2,11.27 17.62,9.85 17.47,8.42C17.92,8.15 18.44,8 19,8M12,14C14.67,14 17,14.89 17,16V19H7V16C7,14.89 9.33,14 12,14Z" /></svg>
                    참여자 ({contract.participants.length}명)
                  </S.InfoSectionTitle>
                  {contract.participants.map((participant) => (
                    <S.InfoItem key={participant.userUuid}>
                      <strong>{getRoleText(participant.role)}:</strong> 
                      {participant.username} ({participant.email})
                    </S.InfoItem>
                  ))}
                </S.InfoSection>
              )}

              {contract.currentVersion && contract.currentVersion.signatures.length > 0 && (
                <S.InfoSection>
                  <S.InfoSectionTitle>
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M19.35,10.04C18.67,6.59 15.64,4 12,4C9.11,4 6.6,5.64 5.35,8.04C2.34,8.36 0,10.91 0,14A6,6 0 0,0 6,20H19A5,5 0 0,0 19.35,10.04M10,17L6,13L7.41,11.59L10,14.17L16.59,7.58L18,9L10,17Z" /></svg>
                    서명 현황 ({contract.currentVersion.signatures.length}명)
                  </S.InfoSectionTitle>
                  {contract.currentVersion.signatures.map((signature) => {
                    const userUuid = currentUser?.uuid || currentUser?.id?.toString();
                    const isCurrentUserSignature = signature.signerUuid === userUuid || signature.signerUuid === currentUser?.id?.toString();
                    return (
                      <S.InfoItem key={signature.signerUuid}>
                        <strong>{signature.signerUsername}:</strong> 
                        {moment(signature.signedAt).format('YYYY년 MM월 DD일 HH:mm')}에 서명
                        {currentUser && isCurrentUserSignature && (
                          <S.StatusBadge statusColor="#388e3c">(본인)</S.StatusBadge>
                        )}
                      </S.InfoItem>
                    );
                  })}
                </S.InfoSection>
              )}
              
              {showVerificationResult && verificationResult && (
                <S.VerificationResultWrapper>
                  <S.VerificationTitleContainer>
                    <S.InfoSectionTitle>
                      <svg width="20" height="20" viewBox="0 0 24 24" fill={verificationResult.overallSuccess ? '#4caf50' : '#f44336'}>
                        <path d="M23,12L20.56,9.22L20.9,5.54L17.29,4.72L15.4,1.54L12,3L8.6,1.54L6.71,4.72L3.1,5.53L3.44,9.21L1,12L3.44,14.78L3.1,18.47L6.71,19.29L8.6,22.47L12,21L15.4,22.46L17.29,19.28L20.9,18.46L20.56,14.78L23,12M10,17L6,13L7.41,11.59L10,14.17L16.59,7.58L18,9L10,17Z"/>
                      </svg>
                      무결성 검증 결과
                    </S.InfoSectionTitle>
                    <S.CloseVerificationButton onClick={() => setShowVerificationResult(false)}>×</S.CloseVerificationButton>
                  </S.VerificationTitleContainer>
                  
                  <S.OverallVerificationBox isSuccess={verificationResult.overallSuccess}>
                    <strong>{verificationResult.overallSuccess ? '✅ 검증 성공' : '❌ 검증 실패'}</strong>
                    <span>{verificationResult.message}</span>
                    <span className="verified-time">검증 시간: {moment(verificationResult.verifiedAt).format('YYYY년 MM월 DD일 HH:mm:ss')}</span>
                  </S.OverallVerificationBox>

                  <S.VerificationStepDetailBox status={verificationResult.dbVerification.status}>
                    <h4>1. DB 기록 무결성 검증</h4>
                    <strong>{getVerificationStatusText(verificationResult.dbVerification.status)}</strong>
                    <div className="details-text">{verificationResult.dbVerification.details}</div>
                    {verificationResult.dbVerification.discrepancies && verificationResult.dbVerification.discrepancies.length > 0 && (
                      <S.DiscrepancyList>
                        {verificationResult.dbVerification.discrepancies.map((item: string, index: number) => (
                          <S.DiscrepancyItem key={`db-disc-${index}`}>{item}</S.DiscrepancyItem>
                        ))}
                      </S.DiscrepancyList>
                    )}
                  </S.VerificationStepDetailBox>

                  <S.VerificationStepDetailBox status={verificationResult.blockchainVerification.status}>
                    <h4>2. 블록체인 데이터 비교 검증</h4>
                    <strong>{getVerificationStatusText(verificationResult.blockchainVerification.status)}</strong>
                    <div className="details-text">{verificationResult.blockchainVerification.details}</div>
                    {verificationResult.blockchainVerification.discrepancies && verificationResult.blockchainVerification.discrepancies.length > 0 && (
                       <S.DiscrepancyList>
                        {verificationResult.blockchainVerification.discrepancies.map((item: string, index: number) => (
                          <S.DiscrepancyItem key={`bc-disc-${index}`}>{item}</S.DiscrepancyItem>
                        ))}
                      </S.DiscrepancyList>
                    )}
                  </S.VerificationStepDetailBox>
                </S.VerificationResultWrapper>
              )}

              {currentUser && statusDisplay && (
                <S.SigningStatusBox 
                    backgroundColor={statusDisplay.backgroundColor} 
                    borderColor={statusDisplay.borderColor} 
                    textColor={statusDisplay.textColor}
                >
                  <strong>{statusDisplay.title}</strong>
                  {statusDisplay.message}
                </S.SigningStatusBox>
              )}
            </>
          )}
          {!contract && !loading && !error && ( // 초기 로딩 전 또는 컨트랙트 ID가 없을 때
             <S.PdfMessage>계약서 정보를 표시할 수 없습니다.</S.PdfMessage>
          )}
        </S.DetailsContainer>
      </S.ModalBodyWrapper>

      <ModalFooter>
        <FooterButton type="button" onClick={handleClose}>닫기</FooterButton>
        {contract && currentUser && canSign() && (
          <FooterButton
            type="button" 
            onClick={handleSign}
            disabled={signing}
            style={{backgroundColor: signing ? '#A5D6A7' : '#4CAF50', color: 'white'}}
          >
            {signing ? '서명 중...' : '서명하기'}
          </FooterButton>
        )}
        {contract && currentUser && hasUserSigned() && !canSign() && (
          <FooterButton 
            type="button" 
            disabled={true}
            style={{backgroundColor: '#4CAF50', color: 'white', opacity: 0.7, cursor: 'not-allowed'}}
          >
            ✅ 서명 완료
          </FooterButton>
        )}
      </ModalFooter>
    </Modal>
  );
};

export default ContractDetailModal;