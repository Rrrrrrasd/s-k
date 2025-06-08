// src/utils/folderApi.ts
import { FolderCreateRequest, FolderUpdateRequest, ContractMoveRequest } from '../types/folder';

const BASE_URL = 'https://localhost:8443/api/folders';

function getAuthHeaders() {
  const token = localStorage.getItem('token');
  return {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
  };
}

// 폴더 생성
export const createFolder = async (request: FolderCreateRequest) => {
  const res = await fetch(BASE_URL, {
    method: 'POST',
    credentials: 'include',
    headers: getAuthHeaders(),
    body: JSON.stringify(request),
  });
  
  if (!res.ok) throw new Error(`폴더 생성 실패: ${res.statusText}`);
  return res.json();
};

// 폴더 목록 조회
export const getFolders = async (parentId?: number, includeChildren = false) => {
  const params = new URLSearchParams();
  if (parentId !== undefined) params.append('parentId', parentId.toString());
  if (includeChildren) params.append('includeChildren', 'true');
  
  const res = await fetch(`${BASE_URL}?${params}`, {
    method: 'GET',
    credentials: 'include',
    headers: getAuthHeaders(),
  });
  
  if (!res.ok) throw new Error(`폴더 목록 조회 실패: ${res.statusText}`);
  return res.json();
};

// 폴더 상세 조회
export const getFolderDetails = async (folderId: number, includeContracts = true) => {
  const params = new URLSearchParams();
  if (includeContracts) params.append('includeContracts', 'true');
  
  const res = await fetch(`${BASE_URL}/${folderId}?${params}`, {
    method: 'GET',
    credentials: 'include',
    headers: getAuthHeaders(),
  });
  
  if (!res.ok) throw new Error(`폴더 상세 조회 실패: ${res.statusText}`);
  return res.json();
};

// 폴더 수정
export const updateFolder = async (folderId: number, request: FolderUpdateRequest) => {
  const res = await fetch(`${BASE_URL}/${folderId}`, {
    method: 'PUT',
    credentials: 'include',
    headers: getAuthHeaders(),
    body: JSON.stringify(request),
  });
  
  if (!res.ok) throw new Error(`폴더 수정 실패: ${res.statusText}`);
  return res.json();
};

// 폴더 삭제
export const deleteFolder = async (folderId: number) => {
  const res = await fetch(`${BASE_URL}/${folderId}`, {
    method: 'DELETE',
    credentials: 'include',
    headers: getAuthHeaders(),
  });
  
  if (!res.ok) throw new Error(`폴더 삭제 실패: ${res.statusText}`);
  return res.json();
};

// 계약서를 폴더로 이동
export const moveContractToFolder = async (request: ContractMoveRequest) => {
  const res = await fetch(`${BASE_URL}/move-contract`, {
    method: 'POST',
    credentials: 'include',
    headers: getAuthHeaders(),
    body: JSON.stringify(request),
  });
  
  if (!res.ok) throw new Error(`계약서 이동 실패: ${res.statusText}`);
  return res.json();
};

// 폴더 트리 구조 조회
export const getFolderTree = async () => {
  const res = await fetch(`${BASE_URL}/tree`, {
    method: 'GET',
    credentials: 'include',
    headers: getAuthHeaders(),
  });
  
  if (!res.ok) throw new Error(`폴더 트리 조회 실패: ${res.statusText}`);
  return res.json();
};

// 폴더 내 계약서 목록 조회
export const getFolderContracts = async (folderId: number) => {
  const res = await fetch(`${BASE_URL}/${folderId}/contracts`, {
    method: 'GET',
    credentials: 'include',
    headers: getAuthHeaders(),
  });
  
  if (!res.ok) throw new Error(`폴더 내 계약서 조회 실패: ${res.statusText}`);
  return res.json();
};