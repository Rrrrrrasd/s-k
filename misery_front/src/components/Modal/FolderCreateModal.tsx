// src/components/Modal/FolderCreateModal.tsx
import React, { useState, useCallback } from 'react';
import Modal from 'react-modal';

// Modal 스타일 관련 임포트
import CustomModal from './Modal'
import {
  ModalHeader, ModalLogo, LogoCircle, CloseButton, ModalBody, ModalTitle,
  ModalDesc, ModalFooter, FooterButton, InputGroup, StyledLabel, StyledInput
} from './styles'

// Props 타입 정의
interface FolderCreateModalProps {
  isOpen: boolean;
  onClose: () => void;
  onCreateSuccess?: () => void;
  parentFolderId?: number | null; // 부모 폴더 ID (null이면 루트에 생성)
  parentFolderName?: string; // 부모 폴더 이름 (UI 표시용)
}

// API 함수
const createFolderApi = async (name: string, parentId?: number) => {
  const token = localStorage.getItem('token');
  const requestBody: any = { name };
  if (parentId) {
    requestBody.parentId = parentId;
  }

  const res = await fetch('https://localhost:8443/api/folders', {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(requestBody),
  });
  
  if (!res.ok) throw new Error(`폴더 생성 실패: ${res.statusText}`);
  return res.json();
};

const FolderCreateModal: React.FC<FolderCreateModalProps> = ({
  isOpen,
  onClose,
  onCreateSuccess,
  parentFolderId,
  parentFolderName,
}) => {
  // --- 모달 내부 상태 관리 ---
  const [folderName, setFolderName] = useState<string>('');
  const [creating, setCreating] = useState<boolean>(false);

  // --- 입력 핸들러 ---
  const handleFolderNameChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setFolderName(event.target.value);
  };

  // --- 내부 닫기 로직 (상태 초기화 및 부모에게 알림) ---
  const handleClose = useCallback(() => {
    // 상태 초기화
    setFolderName('');
    setCreating(false);
    
    onClose(); // 부모 컴포넌트에 닫기 요청 전달
  }, [onClose]);

  // --- 실제 폴더 생성 로직 ---
  const handleCreateFolder = async () => {
    // 유효성 검사
    if (!folderName.trim()) {
      alert('폴더 이름을 입력해주세요.');
      return;
    }

    setCreating(true);
    
    try {
      const response = await createFolderApi(folderName.trim(), parentFolderId || undefined);
      
      if (response.success) {
        alert('폴더가 성공적으로 생성되었습니다.');
        handleClose(); // 모달 닫기
        
        // 성공 콜백 호출 (부모 컴포넌트에서 목록 새로고침 등)
        if (onCreateSuccess) {
          onCreateSuccess();
        }
      } else {
        throw new Error(response.message || '폴더 생성에 실패했습니다.');
      }

    } catch (error) {
      console.error('폴더 생성 오류:', error);
      alert(`폴더 생성 중 오류가 발생했습니다: ${error instanceof Error ? error.message : '알 수 없는 오류'}`);
    } finally {
      setCreating(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onRequestClose={handleClose}
      style={CustomModal}
      contentLabel="새 폴더 만들기"
    >
      <ModalHeader>
        <ModalLogo>
          <LogoCircle>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
              <path d="M10 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z"/>
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
        <ModalTitle>새 폴더 만들기</ModalTitle>
        
        {parentFolderName && (
          <ModalDesc>
            <strong>{parentFolderName}</strong> 폴더 안에 새 폴더를 만듭니다.
          </ModalDesc>
        )}
        
        {!parentFolderName && (
          <ModalDesc>
            루트 폴더에 새 폴더를 만듭니다.
          </ModalDesc>
        )}

        {/* 폴더명 입력 */}
        <InputGroup>
          <StyledLabel htmlFor="folderName">폴더 이름 *</StyledLabel>
          <StyledInput
            type="text"
            id="folderName"
            value={folderName}
            onChange={handleFolderNameChange}
            placeholder="폴더 이름을 입력하세요"
            required
            autoFocus
            onKeyDown={(e) => {
              if (e.key === 'Enter' && !creating && folderName.trim()) {
                handleCreateFolder();
              }
            }}
          />
        </InputGroup>
      </ModalBody>

      <ModalFooter>
        <FooterButton type="button" onClick={handleClose} disabled={creating}>
          취소
        </FooterButton>
        <FooterButton 
          type="button" 
          onClick={handleCreateFolder}
          disabled={creating || !folderName.trim()}
        >
          {creating ? '생성 중...' : '폴더 만들기'}
        </FooterButton>
      </ModalFooter>
    </Modal>
  );
};

export default FolderCreateModal;