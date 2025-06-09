import { memo, useState, useRef, useEffect, useCallback } from 'react';
import { ReactComponent as PlusIcon } from '../../assets/icons/plus.svg';

import {
  NBContainer,
  DropdownContainer,
  DropdownItem
} from './styles';

import ContractUploadModal from '../Modal/Modal_ContractUpload';

interface NewButtonProps {
  onContractUploadSuccess?: () => void;
}

const NewButton = memo(function NewButton({ onContractUploadSuccess }: NewButtonProps) {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isContractUploadModalOpen, setIsContractUploadModalOpen] = useState(false);
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
    // 폴더 생성 로직 (추후 구현)
    console.log('폴더 생성 클릭');
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

  const handleContractUploadSuccess = () => {
    // 업로드 성공 시 콜백 호출
    if (onContractUploadSuccess) {
      onContractUploadSuccess();
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
           <DropdownItem onClick={handleCreateFolder}>폴더 만들기</DropdownItem>
           <DropdownItem onClick={handleContractUpload}>계약서 업로드</DropdownItem>
           <DropdownItem onClick={handleFolderUpload}>폴더 업로드</DropdownItem>
        </DropdownContainer>
      )}

      {/* 계약서 업로드 모달 */}
      <ContractUploadModal
        isOpen={isContractUploadModalOpen}
        onClose={closeContractUploadModal}
        onUploadSuccess={handleContractUploadSuccess}
      />
    </NBContainer>
  );
});

export default NewButton;