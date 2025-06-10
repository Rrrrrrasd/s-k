import React, { useState, useEffect, useCallback } from 'react';
// API 함수들 import
import { getMyContracts, getFavoriteContracts } from '../utils/api';
// 실제 컴포넌트들 import
import ListTile from '../components/ListTile'; // 실제 ListTile 컴포넌트
import Sidebar from '../components/LeftAsideColumn'; // 실제 사이드바 (LeftAsideColumn) 컴포넌트
// ListTileMenu는 ListTile 내부에서 사용될 것이므로 여기서 직접 import 할 필요는 없을 수 있습니다.
// 만약 ListTile이 ListTileMenu를 내부에서 사용하고 export하지 않는다면 여기 import도 필요 없을 수 있습니다.
// 하지만 ListTile 내부 구현을 모르므로 일단 남겨둡니다. 필요 없으면 제거하세요.
import ListTileMenu from '../components/ListTileMenu';

// 타입 정의 (별도 파일로 분리하는 것을 권장: src/types/contract.ts 등)
interface ContractListDTO {
  id: number | string;
  title: string;
  status: string; // 실제 타입에 맞게 (예: 'OPEN' | 'CLOSED' | 'CANCELLED')
  createdAt: string; // ISO 문자열 형태라고 가정
  currentVersionNumber?: number;
  isFavorite: boolean;
  // ListTile 표시에 필요한 추가 데이터? (예: 만든 사람, 수정 일자 등)
  // 이 DTO 안에 ListTile이 필요로 하는 모든 정보가 포함되어야 합니다.
  // 예시:
  // createdByUsername?: string;
  // updatedAt?: string;
  // fileType?: 'FOLDER' | 'PDF' | 'IMAGE'; // 아이콘 표시용? (getIcon 유틸 사용 시 필요할 수 있음)
}

interface PageResponse<T> {
  content: T[];
  totalPages?: number;
  totalElements?: number;
  //... 기타 페이징 정보
}

interface ApiResponse<T> {
    success: boolean;
    message?: string;
    result?: T;
}

const ContractListPage: React.FC = () => {
  const [contracts, setContracts] = useState<ContractListDTO[]>([]);
  const [viewMode, setViewMode] = useState<'all' | 'favorites'>('all');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  // const [currentPage, setCurrentPage] = useState(0);
  // const [totalPages, setTotalPages] = useState(0);

  const loadContracts = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const apiFunc = viewMode === 'all' ? getMyContracts : getFavoriteContracts;
      const response: ApiResponse<PageResponse<ContractListDTO>> = await apiFunc(/* currentPage, 10 */);

      if (response.success && response.result?.content) { // content 존재 여부 확인
        setContracts(response.result.content);
        // setTotalPages(response.result.totalPages ?? 0); // 페이지네이션 상태 업데이트
      } else {
        // 백엔드에서 보내는 에러 메시지가 있다면 사용, 없다면 기본 메시지
        throw new Error(response.message || (viewMode === 'all' ? '내 계약 목록 로딩 실패' : '즐겨찾기 목록 로딩 실패'));
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
      setContracts([]);
    } finally {
      setIsLoading(false);
    }
  }, [viewMode /*, currentPage */]);

  useEffect(() => {
    loadContracts();
  }, [loadContracts]);

  const handleFavoriteToggled = (contractId: string | number, newFavoriteStatus: boolean) => {
    setContracts(prevContracts =>
      prevContracts.map(contract =>
        contract.id === contractId
          ? { ...contract, isFavorite: newFavoriteStatus }
          : contract
      )
    );
    if (viewMode === 'favorites' && !newFavoriteStatus) {
      setContracts(prevContracts => prevContracts.filter(contract => contract.id !== contractId));
    }
  };

  const handleChangeViewMode = (mode: 'all' | 'favorites') => {
    // 이미 같은 모드이면 변경하지 않음 (선택적)
    if (viewMode === mode) return;
    setViewMode(mode);
    // setCurrentPage(0);
  };

  // ContractListDTO 데이터를 ListTile이 받는 props 형태로 변환하는 로직 (예시)
  // 실제 ListTile 컴포넌트의 prop과 DTO 필드에 맞게 수정 필요
  const mapContractToTileProps = (contract: ContractListDTO) => {
    // 예시: getIcon 유틸리티 함수가 있고, contract.fileType 필드가 있다고 가정
    // const icon = getIcon(contract.fileType || 'FILE');
    return {
      key: contract.id, // React 목록 렌더링용 고유 키
      title: contract.title,
      // 예시: 부제(subtitle)로 생성 날짜와 상태 표시
      subtitle: `${new Date(contract.createdAt).toLocaleDateString()} - ${contract.status}`,
      // trailing 속성으로 버전 번호 표시 (없으면 빈 문자열)
      trailing: contract.currentVersionNumber ? `v${contract.currentVersionNumber}` : '',
      // icon: icon, // 아이콘 prop 전달 예시
      // === 중요: 아래 두 prop은 ListTile 컴포넌트가 받도록 수정해야 함 ===
      isFavorite: contract.isFavorite,
      contractId: contract.id, // ListTile 내부에서 ListTileMenu에 전달할 ID
      onFavoriteToggle: handleFavoriteToggled, // 콜백 함수 전달
      // onClick 등 ListTile에 필요한 다른 이벤트 핸들러 prop 전달
      // onClick: () => handleTileClick(contract.id),
    };
  };

  return (
    <div style={{ display: 'flex', height: '100vh' }}> {/* 전체 높이 설정 (예시) */}
      {/* === 중요: Sidebar(LeftAsideColumn)가 받는 실제 prop 확인 필요 === */}
      <Sidebar
        /* LeftAsideColumn의 실제 props 전달 */
        // 아래는 가상의 props, 실제 컴포넌트에 맞게 수정하세요
        currentView={viewMode} // 예시 prop
        onChangeView={handleChangeViewMode} // 예시 prop
      />

      <div style={{ flexGrow: 1, padding: '20px', overflowY: 'auto' }}> {/* 스크롤 추가 */}
        <h1>{viewMode === 'all' ? '내 계약서' : '관심 계약서'}</h1>

        {isLoading && <p>로딩 중...</p>}
        {error && <p style={{ color: 'red' }}>오류: {error}</p>}

        {!isLoading && !error && (
          <div>
            {contracts.length === 0 ? (
              <p>표시할 계약서가 없습니다.</p>
            ) : (
              contracts.map((contract) => (
                // ★ ListTile이 받는 props 형태로 변환하여 전달
                <ListTile {...mapContractToTileProps(contract)} />
              ))
            )}
            {/* 페이지네이션 UI */}
          </div>
        )}
      </div>
    </div>
  );
};

export default ContractListPage;