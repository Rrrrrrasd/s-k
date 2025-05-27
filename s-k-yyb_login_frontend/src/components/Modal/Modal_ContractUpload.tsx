// import React, { useState, useRef, useCallback } from 'react';
// import Modal from 'react-modal';

// // Modal 스타일 관련 임포트 (경로 주의)
// import CustomModal from '../Modal/Modal'
// import {
//   ModalHeader, ModalLogo, LogoCircle, CloseButton, ModalBody, ModalTitle,
//   ModalDesc, UploadArea, UploadIcon, UploadTitle, UploadDesc, ModalFooter,
//   FooterButton, InfoDisplay, InputGroup, StyledLabel, StyledInput
// } from '../Modal/styles'

// // Props 타입 정의
// interface ContractUploadModalProps {
//   isOpen: boolean;
//   onClose: () => void;
//   currentUserIdentifier: string; // 현재 사용자 정보
// }

// const ContractUploadModal: React.FC<ContractUploadModalProps> = ({
//   isOpen,
//   onClose,
//   currentUserIdentifier,
// }) => {
//   // --- 모달 내부 상태 관리 ---
//   const [selectedFile, setSelectedFile] = useState<File | null>(null);
//   const [selectedFileName, setSelectedFileName] = useState<string>('');
//   const [counterparty, setCounterparty] = useState<string>('');

//   // --- Ref ---
//   const fileInputRef = useRef<HTMLInputElement>(null);
//   // const counterpartyInputRef = useRef<HTMLInputElement>(null); // 포커스 필요시 추가

//   // --- 파일 선택 핸들러 ---
//   const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
//     const file = event.target.files?.[0];
//     if (file) {
//       setSelectedFile(file);
//       setSelectedFileName(file.name);
//       console.log('파일 선택됨:', file.name);
//     } else {
//       // 파일 선택 취소 시 초기화 (선택 사항)
//       // setSelectedFile(null);
//       // setSelectedFileName('');
//     }
//      // 파일 선택 후 입력 값 초기화 (다시 같은 파일 선택 가능하도록)
//      if (event.target) {
//         event.target.value = '';
//       }
//   };

//   // --- 피계약자 입력 핸들러 ---
//   const handleCounterpartyChange = (event: React.ChangeEvent<HTMLInputElement>) => {
//     setCounterparty(event.target.value);
//   };

//   // --- 파일 입력 트리거 ---
//   const triggerFileInput = () => {
//     fileInputRef.current?.click();
//   };

//   // --- 내부 닫기 로직 (상태 초기화 및 부모에게 알림) ---
//   const handleClose = useCallback(() => {
//     // 상태 초기화
//     setSelectedFile(null);
//     setSelectedFileName('');
//     setCounterparty('');
//     // 파일 입력 초기화 (선택 사항, 다음 열릴 때 초기화해도 됨)
//     // if (fileInputRef.current) {
//     //   fileInputRef.current.value = '';
//     // }
//     onClose(); // 부모 컴포넌트(NewButton)에 닫기 요청 전달
//   }, [onClose]);

//   // --- 실제 업로드 로직 ---
//   const handleActualUpload = () => {
//     // 유효성 검사
//     if (!selectedFile) {
//       alert('업로드할 계약서 파일(PDF)을 선택해주세요.');
//       return;
//     }
//     if (!counterparty.trim()) {
//       alert('피계약자 정보를 입력해주세요.');
//       // counterpartyInputRef.current?.focus(); // 포커스 필요시
//       return;
//     }

//     const uploadData = {
//       contractor: currentUserIdentifier,
//       counterparty: counterparty.trim(),
//       file: selectedFile,
//     };

//     console.log('업로드 데이터:', uploadData);
//     alert( /* ... 이전과 동일한 alert 메시지 ... */ );

//     handleClose(); // 업로드 성공 후 모달 닫기 (상태 초기화 포함)
//   };


//   return (
//     <Modal
//       isOpen={isOpen} // prop으로 받은 isOpen 사용
//       onRequestClose={handleClose} // prop으로 받은 onClose 호출 (내부 상태 초기화 후)
//       style={CustomModal}
//       contentLabel="계약서 파일 업로드"
//       // ariaHideApp={true}
//     >
//       {/* 모달 내부는 이전 NewButton의 Modal 내용과 거의 동일 */}
//       <ModalHeader>
//         <ModalLogo><LogoCircle>{/* SVG */}</LogoCircle></ModalLogo>
//         {/* 닫기 버튼은 handleClose 호출 */}
//         <CloseButton type="button" onClick={handleClose}>{/* SVG */}</CloseButton>
//       </ModalHeader>

//       <ModalBody>
//         <ModalTitle>계약서 업로드</ModalTitle>
//         <InfoDisplay>
//           <strong>계약자 (본인):</strong> {currentUserIdentifier}
//         </InfoDisplay>
//         <InputGroup>
//           <StyledLabel htmlFor="counterpartyInputModal">피계약자:</StyledLabel>
//           <StyledInput
//             type="text"
//             id="counterpartyInputModal" // ID 중복 피하기 위해 변경
//             value={counterparty}
//             onChange={handleCounterpartyChange}
//             placeholder="피계약자의 이름, ID 또는 주소 입력"
//           />
//         </InputGroup>
//         <ModalDesc>
//           {selectedFileName ? `선택된 파일: ${selectedFileName}` : '업로드할 계약서 파일(PDF)을 선택하거나 드래그하세요.'}
//         </ModalDesc>
//         <input
//           type="file"
//           accept=".pdf"
//           onChange={handleFileChange}
//           ref={fileInputRef}
//           style={{ display: 'none' }}
//         />
//         <UploadArea type="button" onClick={triggerFileInput}>
//            {/* ... 파일 선택 UI (selectedFileName 기반) ... */}
//            {selectedFileName ? ( /* ... */) : ( /* ... */ )}
//         </UploadArea>
//       </ModalBody>

//       <ModalFooter>
//         {/* 취소 버튼은 handleClose 호출 */}
//         <FooterButton type="button" onClick={handleClose}>취소</FooterButton>
//         <FooterButton type="button" onClick={handleActualUpload}>업로드</FooterButton>
//       </ModalFooter>
//     </Modal>
//   );
// };

// export default ContractUploadModal;