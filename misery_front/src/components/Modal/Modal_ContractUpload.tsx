import React, { useState, useRef, useCallback, useEffect } from 'react';
import Modal from 'react-modal';

// Modal 스타일 관련 임포트
import CustomModal from './Modal'
import {
  ModalHeader, ModalLogo, LogoCircle, CloseButton, ModalBody, ModalTitle,
  ModalDesc, UploadArea, UploadIcon, UploadTitle, UploadDesc, ModalFooter,
  FooterButton, InfoDisplay, InputGroup, StyledLabel, StyledInput
} from './styles'

// Props 타입 정의
interface ContractUploadModalProps {
  isOpen: boolean;
  onClose: () => void;
  onUploadSuccess?: () => void; // 업로드 성공 시 콜백
}

// 사용자 타입 정의
interface User {
  id: number;
  uuid: string;
  username: string;
  email: string;
}

// 사용자 검색 API 함수
const searchUsers = async (query: string): Promise<User[]> => {
  const token = localStorage.getItem('token');
  try {
    const res = await fetch(`https://localhost:8443/api/users/search?q=${encodeURIComponent(query)}`, {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
    });
    if (!res.ok) throw new Error(`사용자 검색 실패: ${res.statusText}`);
    const result = await res.json();
    return result.success ? result.data : [];
  } catch (error) {
    console.error('사용자 검색 오류:', error);
    return [];
  }
};

const ContractUploadModal: React.FC<ContractUploadModalProps> = ({
  isOpen,
  onClose,
  onUploadSuccess,
}) => {
  // --- 모달 내부 상태 관리 ---
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [selectedFileName, setSelectedFileName] = useState<string>('');
  const [title, setTitle] = useState<string>('');
  const [description, setDescription] = useState<string>('');
  const [uploading, setUploading] = useState<boolean>(false);

  // 참여자 관련 상태
  const [participantSearchQuery, setParticipantSearchQuery] = useState<string>('');
  const [searchResults, setSearchResults] = useState<User[]>([]);
  const [selectedParticipants, setSelectedParticipants] = useState<User[]>([]);
  const [isSearching, setIsSearching] = useState<boolean>(false);
  const [showSearchResults, setShowSearchResults] = useState<boolean>(false);

  // --- Ref ---
  const fileInputRef = useRef<HTMLInputElement>(null);
  const searchInputRef = useRef<HTMLInputElement>(null);
  const searchResultsRef = useRef<HTMLDivElement>(null);

  // 디바운스된 검색
  useEffect(() => {
    const timeoutId = setTimeout(async () => {
      if (participantSearchQuery.trim().length >= 2) {
        setIsSearching(true);
        try {
          const results = await searchUsers(participantSearchQuery.trim());
          // 이미 선택된 참여자는 제외
          const filteredResults = results.filter(
            user => !selectedParticipants.some(selected => selected.uuid === user.uuid)
          );
          setSearchResults(filteredResults);
          setShowSearchResults(true);
        } catch (error) {
          console.error('검색 실패:', error);
          setSearchResults([]);
        } finally {
          setIsSearching(false);
        }
      } else {
        setSearchResults([]);
        setShowSearchResults(false);
      }
    }, 300);

    return () => clearTimeout(timeoutId);
  }, [participantSearchQuery, selectedParticipants]);

  // 외부 클릭 시 검색 결과 숨기기
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        searchResultsRef.current &&
        !searchResultsRef.current.contains(event.target as Node) &&
        !searchInputRef.current?.contains(event.target as Node)
      ) {
        setShowSearchResults(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

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
    // 파일 선택 후 입력 값 초기화 (다시 같은 파일 선택 가능하도록)
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

  const handleParticipantsChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setParticipantSearchQuery(event.target.value);
  };

  // 참여자 선택
  const handleSelectParticipant = (user: User) => {
    setSelectedParticipants(prev => [...prev, user]);
    setParticipantSearchQuery('');
    setShowSearchResults(false);
    setSearchResults([]);
  };

  // 참여자 제거
  const handleRemoveParticipant = (userUuid: string) => {
    setSelectedParticipants(prev => prev.filter(user => user.uuid !== userUuid));
  };

  // --- 파일 입력 트리거 ---
  const triggerFileInput = () => {
    fileInputRef.current?.click();
  };

  // --- 내부 닫기 로직 (상태 초기화 및 부모에게 알림) ---
  const handleClose = useCallback(() => {
    // 상태 초기화
    setSelectedFile(null);
    setSelectedFileName('');
    setTitle('');
    setDescription('');
    setUploading(false);
    setParticipantSearchQuery('');
    setSearchResults([]);
    setSelectedParticipants([]);
    setShowSearchResults(false);
    
    onClose(); // 부모 컴포넌트에 닫기 요청 전달
  }, [onClose]);

  // --- 참여자 ID 파싱 ---
  const getParticipantIds = (): string[] => {
    return selectedParticipants.map(user => user.uuid);
  };

  // --- 실제 업로드 로직 ---
  const handleActualUpload = async () => {
    // 유효성 검사
    if (!selectedFile) {
      alert('업로드할 계약서 파일(PDF)을 선택해주세요.');
      return;
    }
    if (!title.trim()) {
      alert('계약서 제목을 입력해주세요.');
      return;
    }

    const participantIds = getParticipantIds();

    setUploading(true);
    
    try {
      // FormData 생성
      const formData = new FormData();
      
      // JSON 데이터 부분
      const contractData = {
        title: title.trim(),
        description: description.trim(),
        participantIds: participantIds // UUID 문자열 배열
      };
      
      formData.append('data', new Blob([JSON.stringify(contractData)], {
        type: 'application/json'
      }));
      formData.append('file', selectedFile);

      // API 호출
      const token = localStorage.getItem('token');
      const response = await fetch('https://localhost:8443/api/contracts/upload', {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Authorization': `Bearer ${token}`,
          // Content-Type은 FormData 사용시 브라우저가 자동으로 설정
        },
        body: formData
      });

      if (!response.ok) {
        throw new Error(`업로드 실패: ${response.statusText}`);
      }

      const result = await response.json();
      
      if (result.success) {
        alert('계약서가 성공적으로 업로드되었습니다.');
        handleClose(); // 모달 닫기
        
        // 성공 콜백 호출 (부모 컴포넌트에서 목록 새로고침 등)
        if (onUploadSuccess) {
          onUploadSuccess();
        }
      } else {
        throw new Error(result.message || '업로드에 실패했습니다.');
      }

    } catch (error) {
      console.error('계약서 업로드 오류:', error);
      alert(`계약서 업로드 중 오류가 발생했습니다: ${error instanceof Error ? error.message : '알 수 없는 오류'}`);
    } finally {
      setUploading(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onRequestClose={handleClose}
      style={CustomModal}
      contentLabel="계약서 업로드"
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
        <ModalTitle>계약서 업로드</ModalTitle>
        
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

        {/* 참여자 검색 및 선택 */}
        <InputGroup>
          <StyledLabel htmlFor="participantSearch">피계약자 (선택사항)</StyledLabel>
          
          {/* 선택된 참여자 목록 */}
          {selectedParticipants.length > 0 && (
            <div style={{ 
              marginBottom: '8px',
              display: 'flex',
              flexWrap: 'wrap',
              gap: '8px'
            }}>
              {selectedParticipants.map(participant => (
                <div
                  key={participant.uuid}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    background: '#e8f0fe',
                    border: '1px solid #1967d2',
                    borderRadius: '16px',
                    padding: '4px 8px',
                    fontSize: '0.875rem',
                    color: '#1967d2'
                  }}
                >
                  <span style={{ marginRight: '4px' }}>
                    {participant.username} ({participant.email})
                  </span>
                  <button
                    type="button"
                    onClick={() => handleRemoveParticipant(participant.uuid)}
                    style={{
                      background: 'none',
                      border: 'none',
                      color: '#1967d2',
                      cursor: 'pointer',
                      padding: '0',
                      fontSize: '14px',
                      lineHeight: '1'
                    }}
                  >
                    ×
                  </button>
                </div>
              ))}
            </div>
          )}

          {/* 검색 입력 */}
          <div style={{ position: 'relative' }}>
            <StyledInput
              ref={searchInputRef}
              type="text"
              id="participantSearch"
              value={participantSearchQuery}
              onChange={handleParticipantsChange}
              placeholder="참여자의 이름, 이메일, 또는 UUID를 검색하세요"
              onFocus={() => {
                if (searchResults.length > 0) {
                  setShowSearchResults(true);
                }
              }}
            />
            
            {/* 검색 결과 드롭다운 */}
            {showSearchResults && (
              <div
                ref={searchResultsRef}
                style={{
                  position: 'absolute',
                  top: '100%',
                  left: 0,
                  right: 0,
                  background: 'white',
                  border: '1px solid #e5e5e5',
                  borderRadius: '0.25rem',
                  boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
                  maxHeight: '200px',
                  overflowY: 'auto',
                  zIndex: 1000
                }}
              >
                {isSearching ? (
                  <div style={{ padding: '12px', textAlign: 'center', color: '#666' }}>
                    검색 중...
                  </div>
                ) : searchResults.length > 0 ? (
                  searchResults.map(user => (
                    <div
                      key={user.uuid}
                      onClick={() => handleSelectParticipant(user)}
                      style={{
                        padding: '12px',
                        cursor: 'pointer',
                        borderBottom: '1px solid #f0f0f0'
                      }}
                      onMouseEnter={(e) => {
                        e.currentTarget.style.backgroundColor = '#f8f9fa';
                      }}
                      onMouseLeave={(e) => {
                        e.currentTarget.style.backgroundColor = 'white';
                      }}
                    >
                      <div style={{ fontWeight: 500, fontSize: '0.875rem' }}>
                        {user.username}
                      </div>
                      <div style={{ fontSize: '0.75rem', color: '#666' }}>
                        {user.email}
                      </div>
                      <div style={{ fontSize: '0.75rem', color: '#999' }}>
                        UUID: {user.uuid}
                      </div>
                    </div>
                  ))
                ) : participantSearchQuery.trim().length >= 2 ? (
                  <div style={{ padding: '12px', textAlign: 'center', color: '#666' }}>
                    검색 결과가 없습니다.
                  </div>
                ) : null}
              </div>
            )}
          </div>
          
          <div style={{ fontSize: '0.75rem', color: '#666', marginTop: '4px' }}>
            참여자 이름, 이메일, 또는 UUID로 검색하여 추가하세요. 피계약자 없이도 계약서 생성이 가능합니다.
          </div>
        </InputGroup>

        {/* 파일 선택 */}
        <ModalDesc>
          {selectedFileName ? `선택된 파일: ${selectedFileName}` : '업로드할 계약서 파일(PDF)을 선택하거나 드래그하세요.'}
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
              <UploadTitle>파일이 선택되었습니다</UploadTitle>
              <UploadDesc><strong>다른 파일</strong>을 선택하거나 업로드 버튼을 클릭하세요</UploadDesc>
            </>
          ) : (
            <>
              <UploadIcon>
                <svg width="40" height="40" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z" />
                </svg>
              </UploadIcon>
              <UploadTitle>PDF 파일을 드래그하거나 클릭하여 선택</UploadTitle>
              <UploadDesc>PDF 형식의 계약서 파일만 업로드 가능합니다</UploadDesc>
            </>
          )}
        </UploadArea>
      </ModalBody>

      <ModalFooter>
        <FooterButton type="button" onClick={handleClose} disabled={uploading}>
          취소
        </FooterButton>
        <FooterButton 
          type="button" 
          onClick={handleActualUpload}
          disabled={uploading || !selectedFile || !title.trim()}
        >
          {uploading ? '업로드 중...' : '계약서 업로드'}
        </FooterButton>
      </ModalFooter>
    </Modal>
  );
};

export default ContractUploadModal;