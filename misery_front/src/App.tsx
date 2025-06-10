import React, { useState, useEffect } from 'react';
import moment from 'moment';
import { ReactComponent as FolderIcon } from './assets/icons/folder.svg';
import Header from './components/Header';
import LeftAsideColumn from './components/LeftAsideColumn';
import PathBar from './components/PathBar';
import Tooltip from './components/Tooltip';
import ContractDetailModal from './components/Modal/ContractDetailModal';
import folders from './data/folders.json';
import { AContainer, AContent, AContentTable, AMain, Button  } from './styles'; // AContentTable 사용 확인
import { refreshTokenApi, getMyContracts } from './utils/api';
import { useNavigate } from 'react-router-dom';
import pdfImg from './assets/icons/pdf.png';
import ListTileMenu from './components/ListTileMenu'; // ListTileMenu 임포트

interface ContractListItem {
  id: number;
  title: string;
  status: 'OPEN' | 'CLOSED' | 'CANCELLED';
  createdAt: string;
  currentVersionNumber?: number;
}

function App() {
  const [remainingTime, setRemainingTime] = useState<string>('');
  const [contracts, setContracts] = useState<ContractListItem[]>([]);
  const [contractsLoading, setContractsLoading] = useState(false);
  const [selectedContractId, setSelectedContractId] = useState<number | null>(null);
  const [isContractDetailModalOpen, setIsContractDetailModalOpen] = useState(false);
  const navigate = useNavigate();

  // --- 기존 함수들 (parseJwt, refreshContracts, useEffect 등) 은 그대로 유지 ---
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
  
    // 계약서 목록 로드
    useEffect(() => {
      refreshContracts();
    }, []);
  
    // 계약서 목록 새로고침 함수
    const refreshContracts = async () => {
      try {
        setContractsLoading(true);
        const response = await getMyContracts(0, 50);
        if (response.success) {
          // API 응답 데이터 구조 확인 및 조정 필요
          const contractData = response.data?.content || response.data || []; 
          setContracts(contractData);
        } else {
           console.error('API 요청 실패:', response.message); // 실패 메시지 로깅
           setContracts([]); // 실패 시 빈 배열로 초기화
        }
      } catch (error) {
        console.error('계약서 목록 새로고침 중 오류 발생:', error);
        setContracts([]); // 오류 발생 시 빈 배열로 초기화
      } finally {
        setContractsLoading(false);
      }
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
  
    const handleContractClick = (contractId: number) => {
      // 계약서 상세 모달 열기
      setSelectedContractId(contractId);
      setIsContractDetailModalOpen(true);
    };
  
    const handleCloseContractModal = () => {
      setIsContractDetailModalOpen(false);
      setSelectedContractId(null);
    };
  
    const handleContractUpdate = () => {
      // 계약서 업데이트 후 목록 새로고침
      refreshContracts();
    };
  //-------------------------------------------------------------------

  return (
    <AContainer>
      <Header />
      <LeftAsideColumn onContractUploadSuccess={refreshContracts} />

      <AMain>
        {/* 토큰 갱신 버튼 & 남은 유효시간 */}
        <div style={{ display: 'flex', justifyContent: 'flex-end', padding: '8px 16px' }}>
          <Button onClick={handleWebAuthnRegisterClick}
            style={{ marginRight: 16 }}>PassKey등록</Button>
          <Button onClick={handleRefresh}>로그인 시간 갱신</Button>
          <span style={{ marginLeft: 12, lineHeight: '32px', fontSize: 14 }}>
            남은 시간: {remainingTime}
          </span>
        </div>

        <PathBar />

        <AContent>
          <AContentTable style={{ width: '100%', tableLayout: 'fixed' }}>
            <thead>
              <tr>
                <th style={{ width: '50px' }}></th> {/* 아이콘 */}
                <th style={{ width: '40%' }}>계약서 이름</th>
                <th style={{ width: '15%' }}>계약자</th>
                <th style={{ width: '20%' }}>최종 수정일자</th>
                <th style={{ width: '10%' }}>파일크기</th>
                <th style={{ width: '50px' }}></th> {/* 메뉴 */}
              </tr>
            </thead>

            <tbody>
              {/* 폴더 목록 */}
              {folders.map(folder => (
                <tr key={`folder-${folder.id}`}>
                  <td style={{ textAlign: 'center', verticalAlign: 'middle' }}>
                    <FolderIcon style={{ width: '16px', height: '16px'}}/>
                  </td>
                  <td>
                    <span style={{ 
                        display: 'inline-block', 
                        whiteSpace: 'nowrap', 
                        overflow: 'hidden', 
                        textOverflow: 'ellipsis',
                        maxWidth: '100%', 
                        verticalAlign: 'middle'
                     }}>
                       {folder.name}
                    </span>
                  </td>
                  <td style={{ verticalAlign: 'middle' }}>me</td>
                  <td style={{ verticalAlign: 'middle' }}>{moment(folder.updatedAt).format('MMM DD, yyyy')}</td>
                  <td style={{ verticalAlign: 'middle' }}>-</td>
                  {/* 폴더 메뉴 */}
                  <td style={{ textAlign: 'center', verticalAlign: 'middle' }}>
                    <ListTileMenu fileId={String(folder.id)} itemType="folder" /> 
                  </td>
                </tr>
              ))}

              {/* 계약서 목록 */}
              {contractsLoading ? (
                 <tr><td colSpan={6} style={{ textAlign: 'center', padding: '20px', color: '#5f6368' }}>계약서 로딩 중...</td></tr>
              ) : contracts.length === 0 ? (
                 <tr><td colSpan={6} style={{ textAlign: 'center', padding: '20px', color: '#5f6368' }}>계약서가 없습니다.</td></tr>
              ) : (
                contracts.map(contract => (
                  <tr key={`contract-${contract.id}`}>
                    {/* 아이콘 셀 (클릭 가능) */}
                    <td 
                      style={{ textAlign: 'center', cursor: 'pointer', verticalAlign: 'middle' }} 
                      onClick={() => handleContractClick(contract.id)}
                    >
                      <img src={pdfImg} alt="contract" style={{ width: '16px', height: '16px' }} />
                    </td>
                    {/* 계약서 이름 셀 (클릭 가능, 내용 처리) */}
                    <td 
                      style={{ cursor: 'pointer', verticalAlign: 'middle' }} 
                      onClick={() => handleContractClick(contract.id)}
                    >
                      <span style={{
                        whiteSpace: 'nowrap',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        maxWidth: '100%',
                        display: 'inline-block',
                        verticalAlign: 'middle'
                      }}>
                        {contract.title}
                      </span>
                      {/* 상태 및 버전 정보 */}
                      <span style={{ 
                          marginLeft: '8px', 
                          fontSize: '0.75rem', 
                          color: getStatusColor(contract.status),
                          fontWeight: 500,
                          whiteSpace: 'nowrap'
                        }}>
                          [{getStatusText(contract.status)}]
                      </span>
                      {contract.currentVersionNumber && (
                        <span style={{ 
                          marginLeft: '4px', 
                          fontSize: '0.75rem', 
                          color: '#5f6368',
                          whiteSpace: 'nowrap'
                        }}>
                          v{contract.currentVersionNumber}
                        </span>
                      )}
                    </td>
                    {/* 계약자 셀 */}
                    <td style={{ verticalAlign: 'middle' }}>me</td>
                    {/* 최종 수정일자 셀 */}
                    <td style={{ verticalAlign: 'middle' }}>
                      {moment(contract.createdAt).format('MMM DD, yyyy')}
                    </td>
                    {/* 파일 크기 셀 */}
                    <td style={{ verticalAlign: 'middle' }}>-</td>
                    {/* 계약서 메뉴 */}
                    <td style={{ textAlign: 'center', verticalAlign: 'middle' }}>
                      <ListTileMenu fileId={String(contract.id)} itemType="file" /> 
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
    </AContainer>
  );
}

export default App;