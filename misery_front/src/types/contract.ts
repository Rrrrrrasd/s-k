// src/types/contract.ts
export interface ContractListItem {
    id: number;
    title: string;
    status: 'OPEN' | 'CLOSED' | 'CANCELLED';
    createdAt: string;
    currentVersionNumber?: number;
  }
  
  export interface ContractApiResponse {
    success: boolean;
    data: {
      content: ContractListItem[];
      totalElements: number;
      totalPages: number;
      size: number;
      number: number;
    };
    message?: string;
  }

  export interface ContractListDTO {
    id: number; // ID 타입 확인 (number 가정)
    title: string;
    status: 'OPEN' | 'CLOSED' | 'CANCELLED'; // Enum 값들 확인
    createdAt: string; // ISO 8601 날짜 문자열 형식 가정
    currentVersionNumber?: number;
    isFavorite: boolean; // 즐겨찾기 상태
    // 필요시 다른 필드 추가 (예: 계약자 정보, 파일 크기 등)
    // contractorName?: string;
    // fileSize?: number;
  }
  
  // 페이지네이션 응답 구조 (백엔드 응답과 일치해야 함)
  export interface PageResponse<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    // 기타 페이징 정보 (필요시 추가)
    // size: number;
    // number: number; // 현재 페이지 번호 (0부터 시작)
  }
  
  // 공통 API 응답 구조 (백엔드 응답과 일치해야 함)
  export interface ApiResponse<T> {
    success: boolean;
    data?: T; // 성공 시 데이터 필드명 확인 (data, result 등)
    message?: string; // 실패 시 메시지
    // 필요 시 에러 코드 등 추가
    // errorCode?: string;
  }