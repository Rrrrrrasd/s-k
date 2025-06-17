import { ContractListItem } from "./contract";

// src/types/folder.ts
export interface FolderItem {
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
  
  export interface FolderCreateRequest {
    name: string;
    parentId?: number;
  }
  
  export interface FolderUpdateRequest {
    name?: string;
    parentId?: number;
  }
  
  export interface ContractMoveRequest {
    contractId: number;
    folderId?: number; // null이면 루트로 이동
  }
  
  export interface FolderApiResponse {
    success: boolean;
    data: FolderItem;
    message?: string;
  }
  
  export interface FolderListApiResponse {
    success: boolean;
    data: FolderItem[];
    message?: string;
  }
  
  // 폴더 컨텍스트용 타입
  export interface FolderContextType {
    currentFolder: FolderItem | null;
    breadcrumbs: FolderItem[];
    folders: FolderItem[];
    contracts: ContractListItem[];
    loading: boolean;
    error: string | null;
    
    // 폴더 관련 액션
    createFolder: (request: FolderCreateRequest) => Promise<void>;
    updateFolder: (folderId: number, request: FolderUpdateRequest) => Promise<void>;
    deleteFolder: (folderId: number) => Promise<void>;
    navigateToFolder: (folderId: number | null) => Promise<void>;
    moveContract: (request: ContractMoveRequest) => Promise<void>;
    refreshCurrentFolder: () => Promise<void>;
  }
  
  // 드래그 앤 드롭용 타입
  export interface DragItem {
    type: 'CONTRACT' | 'FOLDER';
    id: number;
    name: string;
  }