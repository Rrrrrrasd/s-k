// src/components/NewButton/index.tsx
import { memo, useState, useRef, useEffect, useCallback } from 'react';
import { ReactComponent as PlusIcon } from '../../assets/icons/plus.svg';

import {
  NBContainer,
  DropdownContainer,
  DropdownItem
} from './styles';

import ContractUploadModal from '../Modal/Modal_ContractUpload';
import FolderCreateModal from '../Modal/FolderCreateModal';

interface NewButtonProps {
  onContractUploadSuccess?: () => void;
  onFolderCreateSuccess?: () => void;
  currentFolderId?: number | null; // 현재 폴더 ID (새 폴더 생성 시 부모로 사용)
  currentFolderName?: string; // 현재 폴더 이름 (UI 표시용)
}

const NewButton = memo(function NewButton({ 
  onContractUploadSuccess, 
  onFolderCreateSuccess,
  currentFolderId,
  currentFolderName 
}: NewButtonProps) {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isContractUploadModalOpen, setIsContractUploadModalOpen] = useState(false);
  const [isFolderCreateModalOpen, setIsFolderCreateModalOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const toggleDropdown = useCallback(() => {
    setIsDropdownOpen((prev) => !prev);
  }, []);

  const handleOutsideClick = useCallback(
    (event: MouseEvent) => {
      const buttonElement = dropdownRef.current?.parentElement?.querySelector('button');
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node) &&
        event.target !== buttonElement &&
        !buttonElement?.contains(event.target as Node)
      ) {
        setIsDropdownOpen(false);
      }
    },
    []
  );

  useEffect(() => {
    if (isDropdownOpen) {
      document.addEventListener('mousedown', handleOutsideClick);
    } else {
      document.removeEventListener('mousedown', handleOutsideClick);
    }

    return () => {
      document.removeEventListener('mousedown', handleOutsideClick);
    };
  }, [isDropdownOpen, handleOutsideClick]);

  const handleCreateFolder = () => {
    setIsDropdownOpen(false);
    setIsFolderCreateModalOpen(true);
  };

  const handleContractUpload = () => {
    setIsDropdownOpen(false);
    setIsContractUploadModalOpen(true);
  };

  const handleFolderUpload = () => {
    setIsDropdownOpen(false);
    // 폴더 업로드 로직 (추후 구현)
    console.log('폴더 업로드 클릭');
  };

  const closeContractUploadModal = () => {
    setIsContractUploadModalOpen(false);
  };

  const closeFolderCreateModal = () => {
    setIsFolderCreateModalOpen(false);
  };

  const handleContractUploadSuccess = () => {
    // 업로드 성공 시 콜백 호출
    if (onContractUploadSuccess) {
      onContractUploadSuccess();
    }
  };

  const handleFolderCreateSuccess = () => {
    // 폴더 생성 성공 시 콜백 호출
    if (onFolderCreateSuccess) {
      onFolderCreateSuccess();
    }
  };

  return (
    <NBContainer>
      <button type="button" onClick={toggleDropdown}>
        <PlusIcon />
        New
      </button>
      
      {isDropdownOpen && (
        <DropdownContainer ref={dropdownRef}>
           <DropdownItem onClick={handleCreateFolder}>
             <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor" style={{ marginRight: '8px' }}>
               <path d="M10 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z"/>
             </svg>
             폴더 만들기
           </DropdownItem>
           <DropdownItem onClick={handleContractUpload}>
             <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor" style={{ marginRight: '8px' }}>
               <path d="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z" />
             </svg>
             계약서 업로드
           </DropdownItem>
           <DropdownItem onClick={handleFolderUpload}>
             <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor" style={{ marginRight: '8px' }}>
               <path d="M14,17H18V14L23,18.5L18,23V20H14V17M13,9H18.5L13,3.5V9M6,2H14L20,8V12.34C19.37,12.12 18.7,12 18,12A6,6 0 0,0 12,18C12,18.7 12.12,19.37 12.34,20H6A2,2 0 0,1 4,18V4A2,2 0 0,1 6,2Z" />
             </svg>
             폴더 업로드
           </DropdownItem>
        </DropdownContainer>
      )}

      {/* 계약서 업로드 모달 */}
      <ContractUploadModal
        isOpen={isContractUploadModalOpen}
        onClose={closeContractUploadModal}
        onUploadSuccess={handleContractUploadSuccess}
      />

      {/* 폴더 생성 모달 */}
      <FolderCreateModal
        isOpen={isFolderCreateModalOpen}
        onClose={closeFolderCreateModal}
        onCreateSuccess={handleFolderCreateSuccess}
        parentFolderId={currentFolderId}
        parentFolderName={currentFolderName}
      />
    </NBContainer>
  );
});

export default NewButton;