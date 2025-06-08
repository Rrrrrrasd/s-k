// src/App.tsx
import React, { useState, useEffect, useRef } from 'react';
import moment from 'moment';
import { ReactComponent as FolderIcon } from './assets/icons/folder.svg';
import Header from './components/Header';
import LeftAsideColumn from './components/LeftAsideColumn';
import FolderCreateModal from './components/Modal/FolderCreateModal';
import PathBar from './components/PathBar';
import Tooltip from './components/Tooltip';
import ContractDetailModal from './components/Modal/ContractDetailModal';
import ContractEditModal from './components/Modal/ContractEditModal';
import { AContainer, AContent, AContentTable, AMain, Button } from './styles';
import { refreshTokenApi, getMyContracts, deleteContract } from './utils/api';
import * as folderApi from './utils/folderApi';
import { useNavigate } from 'react-router-dom';
import pdfImg from './assets/icons/pdf.png';

interface ContractListItem {
  id: number;
  title: string;
  status: 'OPEN' | 'CLOSED' | 'CANCELLED';
  createdAt: string;
  currentVersionNumber?: number;
}

interface FolderItem {
  id: number;
  name: string;
  path: string;
  parentId?: number;
  parentName?: string;
  createdAt: string;
  createdBy: {
    id: number;
    username: string;
    email: string;
  };
  children?: FolderItem[];
  contracts?: ContractListItem[];
  childrenCount: number;
  contractsCount: number;
}

// 3점 메뉴 컴포넌트
interface ContractActionsMenuProps {
  contractId: number;
  contractTitle: string;
  contractStatus: string;
  onEdit: (contractId: number) => void;
  onDelete: (contractId: number) => void;
  onClose: () => void;
}

const ContractActionsMenu: React.FC<ContractActionsMenuProps> = ({
  contractId,
  contractTitle,
  contractStatus,
  onEdit,
  onDelete,
  onClose
}) => {
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        onClose();
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [onClose]);

  const handleEdit = () => {
    onEdit(contractId);
    onClose();
  };

  const handleDelete = () => {
    if (window.confirm(`"${contractTitle}" 계약서를 삭제하시겠습니까?\n\n삭제된 계약서는 복구할 수 없습니다.`)) {
      onDelete(contractId);
      onClose();
    }
  };

  const canEdit = contractStatus === 'OPEN';
  const canDelete = contractStatus !== 'CLOSED';

  return (
    <div
      ref={menuRef}
      style={{
        position: 'absolute',
        top: '100%',
        right: '0',
        backgroundColor: 'white',
        border: '1px solid #e0e0e0',
        borderRadius: '4px',
        boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
        zIndex: 1000,
        minWidth: '120px',
        padding: '4px 0'
      }}
    >
      <button
        onClick={handleEdit}
        disabled={!canEdit}
        style={{
          width: '100%',
          padding: '8px 16px',
          border: 'none',
          background: 'none',
          textAlign: 'left',
          cursor: canEdit ? 'pointer' : 'not-allowed',
          color: canEdit ? '#333' : '#999',
          fontSize: '14px',
          display: 'flex',
          alignItems: 'center',
          gap: '8px'
        }}
        onMouseEnter={(e) => {
          if (canEdit) {
            e.currentTarget.style.backgroundColor = '#f5f5f5';
          }
        }}
        onMouseLeave={(e) => {
          e.currentTarget.style.backgroundColor = 'transparent';
        }}
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
          <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
        </svg>
        수정
      </button>
      
      <button
        onClick={handleDelete}
        disabled={!canDelete}
        style={{
          width: '100%',
          padding: '8px 16px',
          border: 'none',
          background: 'none',
          textAlign: 'left',
          cursor: canDelete ? 'pointer' : 'not-allowed',
          color: canDelete ? '#d32f2f' : '#999',
          fontSize: '14px',
          display: 'flex',
          alignItems: 'center',
          gap: '8px'
        }}
        onMouseEnter={(e) => {
          if (canDelete) {
            e.currentTarget.style.backgroundColor = '#ffebee';
          }
        }}
        onMouseLeave={(e) => {
          e.currentTarget.style.backgroundColor = 'transparent';
        }}
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
          <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
        </svg>
        삭제
      </button>
    </div>
  );
};

function App() {
  const [remainingTime, setRemainingTime] = useState<string>('');
  const [contracts, setContracts] = useState<ContractListItem[]>([]);
  const [contractsLoading, setContractsLoading] = useState(false);
  const [selectedContractId, setSelectedContractId] = useState<number | null>(null);
  const [isContractDetailModalOpen, setIsContractDetailModalOpen] = useState(false);
  const [isContractEditModalOpen, setIsContractEditModalOpen] = useState(false);
  const [editingContractId, setEditingContractId] = useState<number | null>(null);
  const [openMenuId, setOpenMenuId] = useState<number | null>(null);
  
  // 폴더 관련 상태 추가
  const [folders, setFolders] = useState<FolderItem[]>([]);
  const [foldersLoading, setFoldersLoading] = useState(false);
  const [currentFolder, setCurrentFolder] = useState<FolderItem | null>(null);
  const [isFolderCreateModalOpen, setIsFolderCreateModalOpen] = useState(false);
  
  const navigate = useNavigate();

  // JWT 페이로드에서 exp(만료시각) 꺼내기
  function parseJwt(token: string) {
    const base64Url = token.split('.')[1];
    const base64    = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const json      = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(json) as { exp: number };
  }

  // 폴더 목록 로드
  const loadFolders = async (parentId?: number) => {
    try {
      setFoldersLoading(true);
      const response = await folderApi.getFolders(parentId, false);
      if (response.success) {
        setFolders(response.data || []);
        console.log('폴더 목록 로드 완료:', response.data);
      }
    } catch (error) {
      console.error('폴더 목록 로드 실패:', error);
      setFolders([]);
    } finally {
      setFoldersLoading(false);
    }
  };

  // 계약서 목록 로드
  const loadContracts = async () => {
    try {
      setContractsLoading(true);
      const response = await getMyContracts(0, 50);
      if (response.success) {
        setContracts(response.data.content || []);
      }
    } catch (error) {
      console.error('계약서 목록 로드 실패:', error);
      setContracts([]);
    } finally {
      setContractsLoading(false);
    }
  };

  // 초기 데이터 로드
  useEffect(() => {
    loadFolders(); // 루트 폴더들 로드
    loadContracts();
  }, []);

  // 폴더 및 계약서 목록 새로고침 함수
  const refreshData = async () => {
    await Promise.all([
      loadFolders(currentFolder?.id),
      loadContracts()
    ]);
  };

  // 1초마다 남은 유효시간 계산
  useEffect(() => {
    const updateRemaining = () => {
      const token = localStorage.getItem('token');
      if (!token) {
        setRemainingTime('토큰 없음');
        return;
      }
      try {
        const { exp } = parseJwt(token);
        const diff   = exp * 1000 - Date.now();
        if (diff <= 0) {
          setRemainingTime('만료됨');
        } else {
          const h = Math.floor(diff / 3600000);
          const m = Math.floor((diff % 3600000) / 60000);
          const s = Math.floor((diff % 60000) / 1000);
          setRemainingTime(`${m}분 ${s}초`);
        }
      } catch {
        setRemainingTime('유효하지 않음');
      }
    };

    updateRemaining();
    const timer = setInterval(updateRemaining, 1000);
    return () => clearInterval(timer);
  }, []);

  // 토큰 수동 갱신 핸들러
  const handleRefresh = async () => {
    try {
      const refreshToken = localStorage.getItem('refreshToken');
      if (!refreshToken) throw new Error('리프레시 토큰이 없습니다');
      const { data } = await refreshTokenApi();
      localStorage.setItem('token', data.accessToken);
      alert('토큰이 갱신되었습니다');
    } catch (e) {
      console.error(e);
      alert('토큰 갱신에 실패했습니다');
    }
  };

  const handleWebAuthnRegisterClick = () => {
    navigate('/webauthn/register'); 
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'OPEN': return '진행중'
      case 'CLOSED': return '완료'
      case 'CANCELLED': return '취소됨'
      default: return status
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'OPEN': return '#1976d2'
      case 'CLOSED': return '#388e3c'
      case 'CANCELLED': return '#d32f2f'
      default: return '#5f6368'
    }
  };

  // 계약서 클릭 (상세 보기)
  const handleContractClick = (event: React.MouseEvent, contractId: number) => {
    // 3점 메뉴나 액션 버튼 클릭 시에는 상세 모달을 열지 않음
    const target = event.target as HTMLElement;
    if (target.closest('[data-action-menu]') || target.closest('[data-action-button]')) {
      return;
    }
    
    setSelectedContractId(contractId);
    setIsContractDetailModalOpen(true);
  };
  

  // 폴더 클릭 핸들러
  const handleFolderClick = async (folderId: number, folderName: string) => {
    try {
      const response = await folderApi.getFolderDetails(folderId, true);
      if (response.success) {
        setCurrentFolder(response.data);
        // 해당 폴더의 하위 폴더들과 계약서들을 로드
        await loadFolders(folderId);
        setContracts(response.data.contracts || []);
      }
    } catch (error) {
      console.error('폴더 이동 실패:', error);
      alert('폴더를 열 수 없습니다.');
    }
  };

  // 루트로 돌아가기
  const handleBackToRoot = async () => {
    setCurrentFolder(null);
    await loadFolders(); // 루트 폴더들 로드
    await loadContracts(); // 모든 계약서 로드
  };

  // 계약서 상세 모달 닫기
  const handleCloseContractModal = () => {
    setIsContractDetailModalOpen(false);
    setSelectedContractId(null);
  };

  // 계약서 수정 모달 열기
  const handleEditContract = (contractId: number) => {
    setEditingContractId(contractId);
    setIsContractEditModalOpen(true);
  };

  // 계약서 수정 모달 닫기
  const handleCloseEditModal = () => {
    setIsContractEditModalOpen(false);
    setEditingContractId(null);
  };

  // 계약서 삭제
  const handleDeleteContract = async (contractId: number) => {
    try {
      const response = await deleteContract(contractId);
      if (response.success) {
        alert('계약서가 성공적으로 삭제되었습니다.');
        await refreshData(); // 목록 새로고침
      } else {
        alert(response.message || '계약서 삭제에 실패했습니다.');
      }
    } catch (error) {
      console.error('계약서 삭제 오류:', error);
      alert('계약서 삭제 중 오류가 발생했습니다.');
    }
  };

  // 계약서 업데이트 후 처리
  const handleContractUpdate = () => {
    refreshData();
  };

  // 3점 메뉴 토글
  const handleToggleMenu = (event: React.MouseEvent, contractId: number) => {
    event.stopPropagation();
    setOpenMenuId(openMenuId === contractId ? null : contractId);
  };

  // 3점 메뉴 닫기
  const handleCloseMenu = () => {
    setOpenMenuId(null);
  };

  // 폴더 생성 모달 관련
  const handleOpenFolderCreateModal = () => {
    setIsFolderCreateModalOpen(true);
  };

  const handleCloseFolderCreateModal = () => {
    setIsFolderCreateModalOpen(false);
  };

  const handleFolderCreateSuccess = () => {
    refreshData();
  };

  const formatFileSize = (isContract = false) => {
    if (isContract) return '-';
    return '-';
  };

  return (
    <AContainer>
      <Header />
      <LeftAsideColumn 
        onContractUploadSuccess={refreshData}
        onFolderCreateSuccess={handleFolderCreateSuccess}
      />

      <AMain>
        {/* 토큰 갱신 버튼 & 남은 유효시간 */}
        <div style={{ display: 'flex', justifyContent: 'flex-end', padding: '8px 16px' }}>
          <Button onClick={handleOpenFolderCreateModal} style={{ marginRight: 16 }}>
            새 폴더
          </Button>
          <Button onClick={handleWebAuthnRegisterClick} style={{ marginRight: 16 }}>
            PassKey등록
          </Button>
          <Button onClick={handleRefresh}>로그인 시간 갱신</Button>
          <span style={{ marginLeft: 12, lineHeight: '32px', fontSize: 14 }}>
            남은 시간: {remainingTime}
          </span>
        </div>

        <PathBar />

        {/* 브레드크럼 네비게이션 */}
        <div style={{ padding: '8px 16px', fontSize: '14px', color: '#5f6368' }}>
          <button
            onClick={handleBackToRoot}
            style={{
              background: 'none',
              border: 'none',
              color: currentFolder ? '#1976d2' : '#333',
              cursor: 'pointer',
              textDecoration: currentFolder ? 'underline' : 'none'
            }}
          >
            내 Drive
          </button>
          {currentFolder && (
            <>
              <span style={{ margin: '0 8px' }}>/</span>
              <span>{currentFolder.name}</span>
            </>
          )}
        </div>

        <AContent>
          <AContentTable>
            <thead>
              <tr>
                <th>Name</th>
                <th>Owner</th>
                <th>Last modified</th>
                <th>File size</th>
                <th style={{ width: '40px' }}></th> {/* 액션 버튼 컬럼 */}
              </tr>
            </thead>

            <tbody>
              {/* 폴더 목록 */}
              {foldersLoading ? (
                <tr>
                  <td colSpan={5} style={{ textAlign: 'center', padding: '20px', color: '#5f6368' }}>
                    폴더 로딩 중...
                  </td>
                </tr>
              ) : (
                folders.map(folder => (
                  <tr 
                    key={`folder-${folder.id}`}
                    style={{ cursor: 'pointer' }}
                    onClick={() => handleFolderClick(folder.id, folder.name)}
                  >
                    <td>
                      <div><FolderIcon /></div>
                      <span>
                        {folder.name}
                        <span style={{ 
                          marginLeft: '8px', 
                          fontSize: '0.75rem', 
                          color: '#5f6368' 
                        }}>
                          ({folder.contractsCount}개 파일)
                        </span>
                      </span>
                    </td>
                    <td>me</td>
                    <td>{moment(folder.createdAt).format('MMM DD, yyyy')}</td>
                    <td>-</td>
                    <td>
                      {/* 폴더 액션 메뉴는 나중에 추가 */}
                    </td>
                  </tr>
                ))
              )}

              {/* 계약서 목록 */}
              {contractsLoading ? (
                <tr>
                  <td colSpan={5} style={{ textAlign: 'center', padding: '20px', color: '#5f6368' }}>
                    계약서 로딩 중...
                  </td>
                </tr>
              ) : contracts.length === 0 ? (
                <tr>
                  <td colSpan={5} style={{ textAlign: 'center', padding: '20px', color: '#5f6368' }}>
                    계약서가 없습니다.
                  </td>
                </tr>
              ) : (
                contracts.map(contract => (
                  <tr 
                    key={`contract-${contract.id}`}
                    style={{ cursor: 'pointer' }}
                    onClick={(e) => handleContractClick(e, contract.id)}
                  >
                    <td>
                      <div>
                        <img src={pdfImg} alt="contract" />
                      </div>
                      <span>
                        {contract.title}
                        <span style={{ 
                          marginLeft: '8px', 
                          fontSize: '0.75rem', 
                          color: getStatusColor(contract.status),
                          fontWeight: 500
                        }}>
                          [{getStatusText(contract.status)}]
                        </span>
                        {contract.currentVersionNumber && (
                          <span style={{ 
                            marginLeft: '4px', 
                            fontSize: '0.75rem', 
                            color: '#5f6368' 
                          }}>
                            v{contract.currentVersionNumber}
                          </span>
                        )}
                      </span>
                    </td>
                    <td>me</td>
                    <td>{moment(contract.createdAt).format('MMM DD, yyyy')}</td>
                    <td>-</td>
                    <td>
                      <div style={{ position: 'relative' }}>
                        <button
                          data-action-button
                          onClick={(e) => handleToggleMenu(e, contract.id)}
                          style={{
                            background: 'none',
                            border: 'none',
                            cursor: 'pointer',
                            padding: '4px',
                            borderRadius: '4px',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            width: '32px',
                            height: '32px'
                          }}
                          onMouseEnter={(e) => {
                            e.currentTarget.style.backgroundColor = '#f5f5f5';
                          }}
                          onMouseLeave={(e) => {
                            e.currentTarget.style.backgroundColor = 'transparent';
                          }}
                        >
                          <svg width="16" height="16" viewBox="0 0 24 24" fill="#5f6368">
                            <path d="M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"/>
                          </svg>
                        </button>
                        
                        {openMenuId === contract.id && (
                          <div data-action-menu>
                            <ContractActionsMenu
                              contractId={contract.id}
                              contractTitle={contract.title}
                              contractStatus={contract.status}
                              onEdit={handleEditContract}
                              onDelete={handleDeleteContract}
                              onClose={handleCloseMenu}
                            />
                          </div>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </AContentTable>
        </AContent>
      </AMain>

      <Tooltip />

      {/* 계약서 상세 모달 */}
      <ContractDetailModal
        isOpen={isContractDetailModalOpen}
        onClose={handleCloseContractModal}
        contractId={selectedContractId}
        onContractUpdate={handleContractUpdate}
      />

      {/* 계약서 수정 모달 */}
      <ContractEditModal
        isOpen={isContractEditModalOpen}
        onClose={handleCloseEditModal}
        contractId={editingContractId}
        onUpdateSuccess={handleContractUpdate}
      />

      {/* 폴더 생성 모달 */}
      <FolderCreateModal
        isOpen={isFolderCreateModalOpen}
        onClose={handleCloseFolderCreateModal}
        onCreateSuccess={handleFolderCreateSuccess}
        parentFolderId={currentFolder?.id}
        parentFolderName={currentFolder?.name}
      />
    </AContainer>
  );
}

export default App;