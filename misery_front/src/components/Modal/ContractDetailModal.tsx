import React, { useState, useEffect } from 'react';
import Modal from 'react-modal';
import moment from 'moment';

// Modal 스타일 관련 임포트
import CustomModal from './Modal'
import {
  ModalHeader, ModalLogo, LogoCircle, CloseButton, ModalBody, ModalTitle,
  ModalDesc, ModalFooter, FooterButton, InfoDisplay, InputGroup, StyledLabel
} from './styles'

// API 함수 임포트 (실제로는 utils/api에서 임포트)
import { getContractDetails, signContract, getCurrentUser } from '../../utils/api';

// 타입 정의
interface ContractDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  contractId: number | null;
  onContractUpdate?: () => void; // 계약서 업데이트 후 콜백
}

interface ContractDetail {
  id: number;
  title: string;
  description?: string;
  status: 'OPEN' | 'CLOSED' | 'CANCELLED';
  createdAt: string;
  updatedAt?: string;
  createdBy: {
    id: number;
    username: string;
    email: string;
  };
  updatedBy?: {
    id: number;
    username: string;
    email: string;
  };
  currentVersion?: {
    id: number;
    versionNumber: number;
    filePath: string;
    fileHash: string;
    status: 'PENDING_SIGNATURE' | 'SIGNED' | 'ARCHIVED';
    createdAt: string;
    storageProvider: string;
    bucketName: string;
    signatures: Array<{
      signerUuid: string;
      signerUsername: string;
      signedAt: string;
      signatureHash: string;
    }>;
  };
  participants: Array<{
    userUuid: string;
    username: string;
    email: string;
    role: 'INITIATOR' | 'COUNTERPARTY';
  }>;
  versionHistory: Array<{
    id: number;
    versionNumber: number;
    status: string;
    createdAt: string;
    signatures: Array<{
      signerUuid: string;
      signerUsername: string;
      signedAt: string;
    }>;
  }>;
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
  const [currentUser, setCurrentUser] = useState<any>(null);

  // 현재 사용자 정보 로드
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
    }
  }, [isOpen]);

  // 계약서 상세 정보 로드
  useEffect(() => {
    if (isOpen && contractId) {
      loadContractDetails();
    }
  }, [isOpen, contractId]);

  const loadContractDetails = async () => {
    if (!contractId) return;
    
    setLoading(true);
    setError(null);
    
    try {
      const response = await getContractDetails(contractId);
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

  // 계약서 서명
  const handleSign = async () => {
    if (!contractId || !contract) return;
    
    // 서명 확인 다이얼로그
    const confirmSign = window.confirm(
      `"${contract.title}" 계약서에 서명하시겠습니까?\n\n서명 후에는 취소할 수 없습니다.`
    );
    
    if (!confirmSign) return;
    
    setSigning(true);
    
    try {
      const response = await signContract(contractId);
      if (response.success) {
        alert('계약서에 성공적으로 서명했습니다.');
        await loadContractDetails(); // 계약서 정보 새로고침
        if (onContractUpdate) {
          onContractUpdate(); // 부모 컴포넌트에 업데이트 알림
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

  // 모달 닫기
  const handleClose = () => {
    setContract(null);
    setError(null);
    setCurrentUser(null);
    onClose();
  };

  // 현재 사용자가 이미 서명했는지 확인
  const hasUserSigned = () => {
    if (!contract || !contract.currentVersion || !currentUser) return false;
    
    return contract.currentVersion.signatures.some(
      signature => signature.signerUuid === currentUser.uuid
    );
  };

  // 현재 사용자가 참여자인지 확인
  const isUserParticipant = () => {
    if (!contract || !currentUser) return false;
    
    // 생성자이거나 참여자 목록에 있는지 확인
    const isCreator = contract.createdBy.id === currentUser.id;
    const isParticipant = contract.participants.some(
      participant => participant.userUuid === currentUser.uuid
    );
    
    return isCreator || isParticipant;
  };

  // 현재 사용자가 서명할 수 있는지 확인
  const canSign = () => {
    if (!contract || !contract.currentVersion || !currentUser) return false;
    if (contract.status !== 'OPEN') return false;
    if (contract.currentVersion.status !== 'PENDING_SIGNATURE') return false;
    if (hasUserSigned()) return false; // 이미 서명한 경우
    if (!isUserParticipant()) return false; // 참여자가 아닌 경우
    
    return true;
  };

  // 서명 상태 메시지
  const getSigningStatusMessage = () => {
    if (!contract || !currentUser) return '';
    
    if (!isUserParticipant()) {
      return '이 계약서의 참여자가 아닙니다.';
    }
    
    if (hasUserSigned()) {
      return '이미 서명하셨습니다.';
    }
    
    if (contract.status === 'CLOSED') {
      return '이미 완료된 계약서입니다.';
    }
    
    if (contract.status === 'CANCELLED') {
      return '취소된 계약서입니다.';
    }
    
    if (contract.currentVersion?.status === 'SIGNED') {
      return '모든 서명이 완료된 계약서입니다.';
    }
    
    if (contract.currentVersion?.status === 'ARCHIVED') {
      return '보관된 버전입니다.';
    }
    
    return '';
  };

  // 상태 텍스트 변환
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

  // 상태 색상
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'OPEN':
      case 'PENDING_SIGNATURE':
        return '#1976d2';
      case 'CLOSED':
      case 'SIGNED':
        return '#388e3c';
      case 'CANCELLED':
        return '#d32f2f';
      case 'ARCHIVED':
        return '#666';
      default:
        return '#666';
    }
  };

  // 역할 텍스트 변환
  const getRoleText = (role: string) => {
    switch (role) {
      case 'INITIATOR': return '계약자';
      case 'COUNTERPARTY': return '피계약자';
      default: return role;
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onRequestClose={handleClose}
      style={CustomModal}
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

      <ModalBody>
        {loading ? (
          <div style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
            계약서 정보를 불러오는 중...
          </div>
        ) : error ? (
          <div style={{ textAlign: 'center', padding: '40px', color: '#d32f2f' }}>
            {error}
          </div>
        ) : contract ? (
          <>
            <ModalTitle>{contract.title}</ModalTitle>
            
            {/* 계약서 기본 정보 */}
            <InfoDisplay style={{ marginBottom: '1rem' }}>
              <strong>상태:</strong> 
              <span style={{ color: getStatusColor(contract.status), marginLeft: '8px' }}>
                {getStatusText(contract.status)}
              </span>
            </InfoDisplay>

            {contract.description && (
              <InfoDisplay style={{ marginBottom: '1rem' }}>
                <strong>설명:</strong> {contract.description}
              </InfoDisplay>
            )}

            <InfoDisplay style={{ marginBottom: '1rem' }}>
              <strong>생성일:</strong> {moment(contract.createdAt).format('YYYY년 MM월 DD일 HH:mm')}
            </InfoDisplay>

            <InfoDisplay style={{ marginBottom: '1rem' }}>
              <strong>생성자:</strong> {contract.createdBy.username} ({contract.createdBy.email})
            </InfoDisplay>

            {/* 현재 버전 정보 */}
            {contract.currentVersion && (
              <>
                <div style={{ 
                  borderTop: '1px solid #e5e5e5', 
                  paddingTop: '1rem', 
                  marginTop: '1rem' 
                }}>
                  <h3 style={{ 
                    fontSize: '1.1rem', 
                    fontWeight: 600, 
                    marginBottom: '0.5rem',
                    color: '#333'
                  }}>
                    현재 버전 (v{contract.currentVersion.versionNumber})
                  </h3>
                  
                  <InfoDisplay style={{ marginBottom: '0.5rem' }}>
                    <strong>상태:</strong> 
                    <span style={{ color: getStatusColor(contract.currentVersion.status), marginLeft: '8px' }}>
                      {getStatusText(contract.currentVersion.status)}
                    </span>
                  </InfoDisplay>

                  <InfoDisplay style={{ marginBottom: '0.5rem' }}>
                    <strong>업로드일:</strong> {moment(contract.currentVersion.createdAt).format('YYYY년 MM월 DD일 HH:mm')}
                  </InfoDisplay>

                  <InfoDisplay style={{ marginBottom: '0.5rem' }}>
                    <strong>파일 해시:</strong> 
                    <span style={{ 
                      fontFamily: 'monospace', 
                      fontSize: '0.8rem', 
                      background: '#f5f5f5',
                      padding: '2px 4px',
                      borderRadius: '2px',
                      marginLeft: '8px'
                    }}>
                      {contract.currentVersion.fileHash.substring(0, 16)}...
                    </span>
                  </InfoDisplay>
                </div>
              </>
            )}

            {/* 참여자 목록 */}
            {contract.participants.length > 0 && (
              <div style={{ 
                borderTop: '1px solid #e5e5e5', 
                paddingTop: '1rem', 
                marginTop: '1rem' 
              }}>
                <h3 style={{ 
                  fontSize: '1.1rem', 
                  fontWeight: 600, 
                  marginBottom: '0.5rem',
                  color: '#333'
                }}>
                  참여자 ({contract.participants.length}명)
                </h3>
                
                {contract.participants.map((participant, index) => (
                  <InfoDisplay key={participant.userUuid} style={{ marginBottom: '0.5rem' }}>
                    <strong>{getRoleText(participant.role)}:</strong> 
                    {participant.username} ({participant.email})
                  </InfoDisplay>
                ))}
              </div>
            )}

            {/* 서명 현황 */}
            {contract.currentVersion && contract.currentVersion.signatures.length > 0 && (
              <div style={{ 
                borderTop: '1px solid #e5e5e5', 
                paddingTop: '1rem', 
                marginTop: '1rem' 
              }}>
                <h3 style={{ 
                  fontSize: '1.1rem', 
                  fontWeight: 600, 
                  marginBottom: '0.5rem',
                  color: '#333'
                }}>
                  서명 현황 ({contract.currentVersion.signatures.length}명)
                </h3>
                
                {contract.currentVersion.signatures.map((signature, index) => (
                  <InfoDisplay key={signature.signerUuid} style={{ marginBottom: '0.5rem' }}>
                    <strong>{signature.signerUsername}:</strong> 
                    {moment(signature.signedAt).format('YYYY년 MM월 DD일 HH:mm')}에 서명
                    {currentUser && signature.signerUuid === currentUser.uuid && (
                      <span style={{ 
                        color: '#388e3c', 
                        fontWeight: 'bold', 
                        marginLeft: '8px' 
                      }}>
                        (본인)
                      </span>
                    )}
                  </InfoDisplay>
                ))}
              </div>
            )}

            {/* 서명 상태 메시지 */}
            {currentUser && (
              <div style={{ 
                borderTop: '1px solid #e5e5e5', 
                paddingTop: '1rem', 
                marginTop: '1rem' 
              }}>
                {canSign() ? (
                  <div style={{
                    padding: '12px',
                    background: '#e8f5e8',
                    border: '1px solid #4caf50',
                    borderRadius: '4px',
                    color: '#2e7d32'
                  }}>
                    <strong>✓ 서명 가능</strong><br />
                    이 계약서에 서명할 수 있습니다.
                  </div>
                ) : getSigningStatusMessage() && (
                  <div style={{
                    padding: '12px',
                    background: '#fff3e0',
                    border: '1px solid #ff9800',
                    borderRadius: '4px',
                    color: '#f57c00'
                  }}>
                    <strong>ℹ️ {getSigningStatusMessage()}</strong>
                  </div>
                )}
              </div>
            )}
          </>
        ) : (
          <div style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
            계약서를 선택해주세요.
          </div>
        )}
      </ModalBody>

      <ModalFooter>
        <FooterButton type="button" onClick={handleClose}>
          닫기
        </FooterButton>
        
        {contract && currentUser && canSign() && (
          <FooterButton 
            type="button" 
            onClick={handleSign}
            disabled={signing}
            style={{
              backgroundColor: signing ? '#ccc' : '#4caf50',
              borderColor: signing ? '#ccc' : '#4caf50'
            }}
          >
            {signing ? '서명 중...' : '서명하기'}
          </FooterButton>
        )}
      </ModalFooter>
    </Modal>
  );
};

export default ContractDetailModal;