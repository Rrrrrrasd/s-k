import { memo, useState, useRef, useEffect, useCallback } from 'react';
import Modal from 'react-modal';
import { ReactComponent as PlusIcon } from '../../assets/icons/plus.svg'; // 경로 확인

import {
  NBContainer,
  DropdownContainer,
  DropdownItem
} from './styles';
import CustomModal from '../Modal/Modal';
import {
  ModalHeader, ModalLogo, LogoCircle, CloseButton, ModalBody, ModalTitle,
  ModalDesc, UploadArea, UploadIcon, UploadTitle, UploadDesc, ModalFooter,
  FooterButton
} from '../Modal/styles';

const NewButton = memo(function NewButton() {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isUploadModalOpen, setIsUploadModalOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // --- 1. 선택된 파일을 저장할 state 추가 ---
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  // (선택 사항) UI 피드백을 위한 파일 이름 상태
  const [selectedFileName, setSelectedFileName] = useState<string>('');

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
  useEffect(() => { /* ... 이전과 동일 ... */ }, [handleOutsideClick]);
  const handleCreateFolder = () => { /* ... 이전과 동일 ... */ };
  const handleFolderUpload = () => { /* ... 이전과 동일 ... */ };

  const handleFileUpload = () => {
    setIsDropdownOpen(false);
    // 모달 열릴 때 이전 선택 파일 정보 초기화
    setSelectedFile(null);
    setSelectedFileName('');
    if (fileInputRef.current) {
      fileInputRef.current.value = ''; // 파일 입력 값 초기화
    }
    setIsUploadModalOpen(true);
  };

  // --- 2. handleFileChange 수정: 파일 선택 시 state에 저장 ---
  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      setSelectedFile(file); // state에 파일 저장
      setSelectedFileName(file.name); // state에 파일 이름 저장 (UI 표시용)
      console.log('파일 선택됨:', file.name); // 선택 확인 로그
      // 여기서는 업로드 로직 실행 안 함!
    } else {
      // 파일 선택 취소 시 초기화 (선택 사항)
      setSelectedFile(null);
      setSelectedFileName('');
    }
  };

  const closeUploadModal = () => {
    setIsUploadModalOpen(false);
    // 모달 닫을 때도 선택 파일 정보 초기화 (선택 사항)
    setSelectedFile(null);
    setSelectedFileName('');
  };

  const triggerFileInput = () => {
    // 파일 선택 창 열기 전에 이전 파일 입력 값 초기화 (선택 사항)
    // if (fileInputRef.current) {
    //   fileInputRef.current.value = '';
    // }
    fileInputRef.current?.click();
  };

  // --- 3. handleActualUpload 수정: "Upload File" 버튼 클릭 시 실행 ---
  const handleActualUpload = () => {
    if (selectedFile) {
      // state에 저장된 파일이 있을 경우 업로드 시뮬레이션 실행
      console.log('업로드 시작할 파일:', selectedFile);
      console.log(`백엔드로 ${selectedFile.name} 파일 전송 시뮬레이션...`);
      alert(`${selectedFile.name} 파일을 백엔드로 전송합니다. (구현 예정)`);

      closeUploadModal(); // 모달 닫기

      // (중요) 업로드 "성공" 후 파일 입력 값 및 상태 초기화
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
      setSelectedFile(null);
      setSelectedFileName('');

    } else {
      // state에 파일이 없을 경우 (선택된 파일이 없을 경우)
      alert('먼저 업로드할 파일을 선택해주세요.');
    }
  };


  return (
    <NBContainer>
      {/* ... 버튼 및 드롭다운 JSX ... */}
      <button type="button" onClick={toggleDropdown}>
        <PlusIcon />
        New
      </button>
      {isDropdownOpen && (
        <DropdownContainer ref={dropdownRef}>
           <DropdownItem onClick={handleCreateFolder}>폴더 만들기</DropdownItem>
           <DropdownItem onClick={handleFileUpload}>파일 업로드</DropdownItem>
           <DropdownItem onClick={handleFolderUpload}>폴더 업로드</DropdownItem>
        </DropdownContainer>
      )}

      <Modal
        isOpen={isUploadModalOpen}
        onRequestClose={closeUploadModal}
        style={CustomModal}
        contentLabel="Upload Files Modal"
      >
        <ModalHeader>
          <ModalLogo>
            <LogoCircle>{/* ... SVG ... */}</LogoCircle>
          </ModalLogo>
          <CloseButton type="button" onClick={closeUploadModal}>{/* ... SVG ... */}</CloseButton>
        </ModalHeader>

        <ModalBody>
          <ModalTitle>Upload Files</ModalTitle>
          <ModalDesc>
            {/* 선택된 파일 이름 표시 (선택 사항) */}
            {selectedFileName ? `Selected: ${selectedFileName}` : 'Attach the file you want to upload below.'}
          </ModalDesc>

          <input
            type="file"
            accept=".pdf"
            onChange={handleFileChange} // handleFileChange는 state 업데이트만 담당
            ref={fileInputRef}
            style={{ display: 'none' }}
          />

          <UploadArea type="button" onClick={triggerFileInput}>
            {/* 파일이 선택되면 다른 내용을 보여줄 수도 있음 (선택 사항) */}
            {selectedFileName ? (
              <>
                <UploadTitle>File Selected</UploadTitle>
                <UploadDesc>Click Upload File below or choose <strong>another file</strong></UploadDesc>
              </>
            ) : (
              <>
                <UploadIcon>{/* ... SVG ... */}</UploadIcon>
                <UploadTitle>Drag File(s) here to Upload</UploadTitle>
                <UploadDesc>Alternatively, select by Clicking <strong>Here</strong></UploadDesc>
              </>
            )}
          </UploadArea>
        </ModalBody>

        <ModalFooter>
          <FooterButton type="button" onClick={closeUploadModal}>Cancel</FooterButton>
          {/* "Upload File" 버튼 클릭 시 handleActualUpload 함수 호출 */}
          <FooterButton type="button" onClick={handleActualUpload}>Upload File</FooterButton>
        </ModalFooter>
      </Modal>
    </NBContainer>
  );
});

export default NewButton;