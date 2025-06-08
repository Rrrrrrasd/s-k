// src/contexts/FolderContext.tsx
import React, { createContext, useContext, useState, useCallback, useEffect } from 'react';
import { FolderContextType, FolderItem, FolderCreateRequest, FolderUpdateRequest, ContractMoveRequest } from '../types/folder';
import { ContractListItem } from '../types/contract';
import * as folderApi from '../utils/folderApi';

const FolderContext = createContext<FolderContextType | undefined>(undefined);

export const useFolderContext = () => {
  const context = useContext(FolderContext);
  if (!context) {
    throw new Error('useFolderContext must be used within a FolderProvider');
  }
  return context;
};

interface FolderProviderProps {
  children: React.ReactNode;
}

export const FolderProvider: React.FC<FolderProviderProps> = ({ children }) => {
  const [currentFolder, setCurrentFolder] = useState<FolderItem | null>(null);
  const [breadcrumbs, setBreadcrumbs] = useState<FolderItem[]>([]);
  const [folders, setFolders] = useState<FolderItem[]>([]);
  const [contracts, setContracts] = useState<ContractListItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 브레드크럼 생성
  const generateBreadcrumbs = useCallback((folder: FolderItem | null): FolderItem[] => {
    if (!folder) return [];
    
    const breadcrumbs: FolderItem[] = [];
    const current = folder;
    
    while (current) {
      breadcrumbs.unshift(current);
      // 부모 폴더는 별도로 조회해야 하므로 일단 현재 폴더만 추가
      break;
    }
    
    return breadcrumbs;
  }, []);

  // 폴더로 이동
  const navigateToFolder = useCallback(async (folderId: number | null) => {
    setLoading(true);
    setError(null);
    
    try {
      if (folderId === null) {
        // 루트로 이동
        const response = await folderApi.getFolders(undefined, false);
        setFolders(response.data);
        setCurrentFolder(null);
        setBreadcrumbs([]);
        setContracts([]);
      } else {
        // 특정 폴더로 이동
        const [folderResponse, childrenResponse] = await Promise.all([
          folderApi.getFolderDetails(folderId, true),
          folderApi.getFolders(folderId, false)
        ]);
        
        setCurrentFolder(folderResponse.data);
        setFolders(childrenResponse.data);
        setContracts(folderResponse.data.contracts || []);
        setBreadcrumbs(generateBreadcrumbs(folderResponse.data));
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '폴더 조회 중 오류가 발생했습니다.';
      setError(errorMessage);
      console.error('폴더 이동 오류:', err);
    } finally {
      setLoading(false);
    }
  }, [generateBreadcrumbs]);

  // 현재 폴더 새로고침
  const refreshCurrentFolder = useCallback(async () => {
    const currentFolderId = currentFolder?.id || null;
    await navigateToFolder(currentFolderId);
  }, [currentFolder?.id, navigateToFolder]);

  // 폴더 생성
  const createFolder = useCallback(async (request: FolderCreateRequest) => {
    try {
      setError(null);
      const response = await folderApi.createFolder(request);
      
      if (response.success) {
        await refreshCurrentFolder();
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '폴더 생성 중 오류가 발생했습니다.';
      setError(errorMessage);
      throw err;
    }
  }, [refreshCurrentFolder]);

  // 폴더 수정
  const updateFolder = useCallback(async (folderId: number, request: FolderUpdateRequest) => {
    try {
      setError(null);
      const response = await folderApi.updateFolder(folderId, request);
      
      if (response.success) {
        await refreshCurrentFolder();
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '폴더 수정 중 오류가 발생했습니다.';
      setError(errorMessage);
      throw err;
    }
  }, [refreshCurrentFolder]);

  // 폴더 삭제
  const deleteFolder = useCallback(async (folderId: number) => {
    try {
      setError(null);
      const response = await folderApi.deleteFolder(folderId);
      
      if (response.success) {
        await refreshCurrentFolder();
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '폴더 삭제 중 오류가 발생했습니다.';
      setError(errorMessage);
      throw err;
    }
  }, [refreshCurrentFolder]);

  // 계약서 이동
  const moveContract = useCallback(async (request: ContractMoveRequest) => {
    try {
      setError(null);
      const response = await folderApi.moveContractToFolder(request);
      
      if (response.success) {
        await refreshCurrentFolder();
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '계약서 이동 중 오류가 발생했습니다.';
      setError(errorMessage);
      throw err;
    }
  }, [refreshCurrentFolder]);

  // 초기 로드 (루트 폴더)
  useEffect(() => {
    navigateToFolder(null);
  }, [navigateToFolder]);

  const value: FolderContextType = {
    currentFolder,
    breadcrumbs,
    folders,
    contracts,
    loading,
    error,
    createFolder,
    updateFolder,
    deleteFolder,
    navigateToFolder,
    moveContract,
    refreshCurrentFolder,
  };

  return (
    <FolderContext.Provider value={value}>
      {children}
    </FolderContext.Provider>
  );
};