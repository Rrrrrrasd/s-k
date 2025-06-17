import React, { useState, useRef, useCallback, useEffect } from 'react';
import Modal from 'react-modal';

// Modal 스타일 관련 임포트
import CustomModal from './Modal'
import {
  ModalHeader, ModalLogo, LogoCircle, CloseButton, ModalBody, ModalTitle,
  ModalDesc, UploadArea, UploadIcon, UploadTitle, UploadDesc, ModalFooter,
  FooterButton, InfoDisplay, InputGroup, StyledLabel, StyledInput
} from './styles'

// API 함수들
import { getContractDetails, updateContract } from '../../utils/api';

// Props 타입 정의
interface ContractEditModalProps {
  isOpen: boolean;
  onClose: () => void;
  contractId: number | null;
  onUpdateSuccess?: () => void;
}

// 계약서 상세 타입 (간단 버전)
interface ContractDetail {
  id: number;
  title: string;
  description?: string;
  status: 'OPEN' | 'CLOSED' | 'CANCELLED';
}

const ContractEditModal: React.FC<ContractEditModalProps> = ({
  isOpen,
  onClose,
  contractId,
  onUpdateSuccess,
}) => {
  // --- 모달 내부 상태 관리 ---
  const [contract, setContract] = useState<ContractDetail | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [selectedFileName, setSelectedFileName] = useState<string>('');
  const [title, setTitle] = useState<string>('');
  const [description, setDescription] = useState<string>('');
  const [updating, setUpdating] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // --- Ref ---
  const fileInputRef = useRef<HTMLInputElement>(null);

  // 계약서 정보 로드
  useEffect(() => {
    if (isOpen && contractId) {
      loadContractInfo();
    }
  }, [isOpen, contractId]);

  const loadContractInfo = async () => {
    if (!contractId) return;
    
    setLoading(true);
    setError(null);
    
    try {
      const response = await getContractDetails(contractId);
      if (response.success) {
        const contractData = response.data;
        setContract(contractData);
        setTitle(contractData.title || '');
        setDescription(contractData.description || '');
      } else {
        setError(response.message || '계약서 정보를 불러올 수 없습니다.');
      }
    } catch (err) {
      setError('계약서 정보를 불러오는 중 오류가 발생했습니다.');
      console.error('계약서 정보 로드 오류:', err);
    } finally {
      setLoading(false);
    }
  };

  // --- 파일 선택 핸들러 ---
  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      // PDF 파일만 허용
      if (file.type !== 'application/pdf') {
        alert('PDF 파일만 업로드 가능합니다.');
        return;
      }
      setSelectedFile(file);
      setSelectedFileName(file.name);
      console.log('파일 선택됨:', file.name);
    }
    // 파일 선택 후 입력 값 초기화
    if (event.target) {
      event.target.value = '';
    }
  };

  // --- 입력 핸들러들 ---
  const handleTitleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setTitle(event.target.value);
  };

  const handleDescriptionChange = (event: React.ChangeEvent<HTMLTextAreaElement>) => {
    setDescription(event.target.value);
  };

  // --- 파일 입력 트리거 ---
  const triggerFileInput = () => {
    fileInputRef.current?.click();
  };

  // --- 내부 닫기 로직 (상태 초기화 및 부모에게 알림) ---
  const handleClose = useCallback(() => {
    // 상태 초기화
    setContract(null);
    setSelectedFile(null);
    setSelectedFileName('');
    setTitle('');
    setDescription('');
    setUpdating(false);
    setLoading(false);
    setError(null);
    
    onClose(); // 부모 컴포넌트에 닫기 요청 전달
  }, [onClose]);

  // --- 실제 수정 로직 ---
  const handleActualUpdate = async () => {
    if (!contractId || !contract) {
      alert('계약서 정보가 없습니다.');
      return;
    }

    // 수정할 내용이 있는지 확인
    const titleChanged = title.trim() !== contract.title;
    const descriptionChanged = description.trim() !== (contract.description || '');
    const fileChanged = selectedFile !== null;

    if (!titleChanged && !descriptionChanged && !fileChanged) {
      alert('수정할 내용이 없습니다.');
      return;
    }

    // 유효성 검사
    if (!title.trim()) {
      alert('계약서 제목을 입력해주세요.');
      return;
    }

    // 파일이 변경되지 않은 경우 확인
    if (!fileChanged) {
      const confirmUpdate = window.confirm(
        '파일을 변경하지 않고 제목/설명만 수정하시겠습니까?\n\n파일을 변경하면 새로운 버전이 생성됩니다.'
      );
      if (!confirmUpdate) return;
    }

    setUpdating(true);
    
    try {
      // FormData 생성
      const formData = new FormData();
      
      // JSON 데이터 부분
      const contractData = {
        title: title.trim(),
        description: description.trim()
      };
      
      formData.append('data', new Blob([JSON.stringify(contractData)], {
        type: 'application/json'
      }));

      // 파일이 선택된 경우에만 파일 추가
      if (selectedFile) {
        formData.append('file', selectedFile);
      } else {
        // 기존 파일을 유지하기 위해 빈 파일을 전송하거나, 
        // 백엔드에서 파일이 없으면 기존 파일을 유지하도록 구현해야 함
        // 여기서는 파일이 없으면 에러를 발생시키도록 함
        alert('파일을 선택해주세요. 계약서 수정 시에는 새로운 PDF 파일이 필요합니다.');
        return;
      }

      // API 호출
      const token = localStorage.getItem('token');
      const response = await fetch(`https://localhost:8443/api/contracts/${contractId}`, {
        method: 'PUT',
        credentials: 'include',
        headers: {
          'Authorization': `Bearer ${token}`,
          // Content-Type은 FormData 사용시 브라우저가 자동으로 설정
        },
        body: formData
      });

      if (!response.ok) {
        throw new Error(`수정 실패: ${response.statusText}`);
      }

      const result = await response.json();
      
      if (result.success) {
        alert('계약서가 성공적으로 수정되었습니다.');
        handleClose(); // 모달 닫기
        
        // 성공 콜백 호출 (부모 컴포넌트에서 목록 새로고침 등)
        if (onUpdateSuccess) {
          onUpdateSuccess();
        }
      } else {
        throw new Error(result.message || '수정에 실패했습니다.');
      }

    } catch (error) {
      console.error('계약서 수정 오류:', error);
      alert(`계약서 수정 중 오류가 발생했습니다: ${error instanceof Error ? error.message : '알 수 없는 오류'}`);
    } finally {
      setUpdating(false);
    }
  };

  // 수정 가능한 상태인지 확인
  const canEdit = contract?.status === 'OPEN';

  return (
    <Modal
      isOpen={isOpen}
      onRequestClose={handleClose}
      style={CustomModal}
      contentLabel="계약서 수정"
    >
      <ModalHeader>
        <ModalLogo>
          <LogoCircle>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
              <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
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
        ) : !contract ? (
          <div style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
            계약서를 선택해주세요.
          </div>
        ) : !canEdit ? (
          <div style={{ textAlign: 'center', padding: '40px', color: '#d32f2f' }}>
            이 계약서는 수정할 수 없습니다. (상태: {contract.status})
          </div>
        ) : (
          <>
            <ModalTitle>계약서 수정</ModalTitle>
            
            <InfoDisplay style={{ marginBottom: '1rem', padding: '12px', backgroundColor: '#fff3e0', border: '1px solid #ff9800', borderRadius: '4px' }}>
              <strong>⚠️ 주의사항:</strong><br />
              계약서를 수정하면 새로운 버전이 생성되고, 기존 서명은 무효화됩니다.<br />
              수정 후 모든 참여자가 다시 서명해야 합니다.
            </InfoDisplay>
            
            {/* 계약서 제목 입력 */}
            <InputGroup>
              <StyledLabel htmlFor="contractTitle">계약서 제목 *</StyledLabel>
              <StyledInput
                type="text"
                id="contractTitle"
                value={title}
                onChange={handleTitleChange}
                placeholder="계약서 제목을 입력하세요"
                required
              />
            </InputGroup>

            {/* 계약서 설명 입력 */}
            <InputGroup>
              <StyledLabel htmlFor="contractDescription">계약서 설명</StyledLabel>
              <StyledInput
                as="textarea"
                id="contractDescription"
                value={description}
                onChange={handleDescriptionChange}
                placeholder="계약서에 대한 간단한 설명을 입력하세요 (선택사항)"
                style={{
                  minHeight: '80px',
                  resize: 'vertical',
                  fontFamily: 'inherit'
                }}
              />
            </InputGroup>

            {/* 파일 선택 */}
            <ModalDesc>
              {selectedFileName ? `선택된 파일: ${selectedFileName}` : '새로운 계약서 파일(PDF)을 선택하세요.'}
            </ModalDesc>

            <input
              type="file"
              accept=".pdf"
              onChange={handleFileChange}
              ref={fileInputRef}
              style={{ display: 'none' }}
            />

            <UploadArea type="button" onClick={triggerFileInput}>
              {selectedFileName ? (
                <>
                  <UploadIcon>
                    <svg width="40" height="40" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z" />
                    </svg>
                  </UploadIcon>
                  <UploadTitle>새 파일이 선택되었습니다</UploadTitle>
                  <UploadDesc><strong>다른 파일</strong>을 선택하거나 수정 버튼을 클릭하세요</UploadDesc>
                </>
              ) : (
                <>
                  <UploadIcon>
                    <svg width="40" height="40" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z" />
                    </svg>
                  </UploadIcon>
                  <UploadTitle>새 PDF 파일을 드래그하거나 클릭하여 선택</UploadTitle>
                  <UploadDesc>수정할 PDF 형식의 계약서 파일을 업로드하세요</UploadDesc>
                </>
              )}
            </UploadArea>
          </>
        )}
      </ModalBody>

      <ModalFooter>
        <FooterButton type="button" onClick={handleClose} disabled={updating}>
          취소
        </FooterButton>
        {canEdit && (
          <FooterButton 
            type="button" 
            onClick={handleActualUpdate}
            disabled={updating || !title.trim() || !selectedFile}
          >
            {updating ? '수정 중...' : '계약서 수정'}
          </FooterButton>
        )}
      </ModalFooter>
    </Modal>
  );
};

export default ContractEditModal;