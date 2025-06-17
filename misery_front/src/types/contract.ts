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