import { bufferToBase64url } from "./webauthn";

// src/utils/api.ts
import Cookies from 'js-cookie';
export const BASE_URL = 'https://localhost:8443/auth';

function defaultHeaders() {
  const csrf = Cookies.get('XSRF-TOKEN');  // Spring이 발급한 CSRF 토큰 쿠키
  return {
    'Content-Type': 'application/json',
    ...(csrf ? { 'X-XSRF-TOKEN': csrf } : {})
  };
}

// 로그인 (기존에 있던 함수)
export const loginApi = (email: string, password: string) =>
  fetch(`${BASE_URL}/login`, {
    method: 'POST',
    credentials: 'include',
    headers: defaultHeaders(),
    body: JSON.stringify({ 
      email, 
      password    // ⚠️ 필드 이름을 password로 맞춰야 합니다
    }),
  }).then(res => {
    if (!res.ok) throw new Error(`로그인 실패: ${res.statusText}`);
    return res.json();
  });

export const signupApi = async (username: string, email: string, password: string) => {
  const res = await fetch(`${BASE_URL}/signup`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, email, password }),
  });
  return res.json();
};

export const getUserInfo = async () => {
    const token = localStorage.getItem('token');
    const res = await fetch('http://localhost:8080/auth/me', {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return res.json();
  };

export const getWebAuthnRegisterOptions = async () => {
      const token = localStorage.getItem('token');
      const res = await fetch(`${BASE_URL}/webauthn/register/options`, {
      method: 'GET',
        credentials: 'include',    // 쿠키 포함
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        }, // CSRF 토큰 헤더 + Content-Type
      });
      if (!res.ok) throw new Error(`등록 옵션 조회 실패: ${res.statusText}`);
      return res.json();
    };

export const verifyWebAuthnRegister = async (payload: any) => {
    const res = await fetch(`${BASE_URL}/webauthn/register/verify`, {
      method: 'POST',
      credentials: 'include',        // 쿠키 포함
      headers: defaultHeaders(),     // CSRF 토큰 헤더 + Content-Type
      body: JSON.stringify(payload),
    });
    if (!res.ok) throw new Error(`등록 검증 실패: ${res.statusText}`);
    return res.json();
  };

  export const getWebAuthnLoginOptions = async (email: string) => {
      const res = await fetch(
        `${BASE_URL}/webauthn/login/options?email=${encodeURIComponent(email)}`,
        {
          method: 'GET',
          credentials: 'include',
          headers: defaultHeaders(),
        }
      );
      if (!res.ok) throw new Error(`옵션 요청 실패: ${res.statusText}`);
      return res.json();
    };

export const verifyWebAuthnLogin = async (payload: {
    request: any;
    response: any;
  }) => {
    const res = await fetch(`${BASE_URL}/webauthn/login/verify`, {
      method: 'POST',
      credentials: 'include',
      headers: defaultHeaders(),
      body: JSON.stringify(payload),
    });
    if (!res.ok) {
      throw new Error(`WebAuthn 로그인 검증 실패: ${res.statusText}`);
    }
    return res.json();
  };

// 리프레시
export const refreshTokenApi = () =>
  fetch(`${BASE_URL}/refresh`, {
    method: 'POST',
    credentials: 'include',
    headers: defaultHeaders()
  }).then(r => r.json());

export const logoutApi = () =>
  fetch(`${BASE_URL}/logout`, {
    method: 'POST',
    credentials: 'include',     // HttpOnly 쿠키 전송
    headers: defaultHeaders(),  // CSRF 토큰 헤더 포함
  }).then(res => {
    if (!res.ok) throw new Error(`로그아웃 실패: ${res.statusText}`);
    return res.json();
  });


// 계약서 목록 조회
export const getMyContracts = async (page = 0, size = 10) => {
  const token = localStorage.getItem('token');
  const res = await fetch(`https://localhost:8443/api/contracts?page=${page}&size=${size}`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      ...defaultHeaders(),
      Authorization: `Bearer ${token}`,
    },
  });
  if (!res.ok) throw new Error(`계약서 목록 조회 실패: ${res.statusText}`);
  return res.json();
};

// 계약서 상세 조회
export const getContractDetails = async (contractId: number) => {
  const token = localStorage.getItem('token');
  const res = await fetch(`https://localhost:8443/api/contracts/${contractId}`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      ...defaultHeaders(),
      Authorization: `Bearer ${token}`,
    },
  });
  if (!res.ok) throw new Error(`계약서 상세 조회 실패: ${res.statusText}`);
  return res.json();
};

// 사용자 검색
export const searchUsers = async (query: string) => {
  const token = localStorage.getItem('token');
  const res = await fetch(`https://localhost:8443/api/users/search?q=${encodeURIComponent(query)}`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      ...defaultHeaders(),
      Authorization: `Bearer ${token}`,
    },
  });
  if (!res.ok) throw new Error(`사용자 검색 실패: ${res.statusText}`);
  return res.json();
};

// 계약서 서명
export const signContract = async (contractId: number) => {
  const token = localStorage.getItem('token');
  const res = await fetch(`https://localhost:8443/api/contracts/${contractId}/sign`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      ...defaultHeaders(),
      Authorization: `Bearer ${token}`,
    },
  });
  if (!res.ok) throw new Error(`계약서 서명 실패: ${res.statusText}`);
  return res.json();
};

// 현재 사용자 정보 조회
export const getCurrentUser = async () => {
  const token = localStorage.getItem('token');
  const res = await fetch('https://localhost:8443/auth/me', {
    method: 'GET',
    credentials: 'include',
    headers: {
      ...defaultHeaders(),
      Authorization: `Bearer ${token}`,
    },
  });
  if (!res.ok) throw new Error(`사용자 정보 조회 실패: ${res.statusText}`);
  return res.json();
};

export const getContractPreviewBlob = async (filePath: string): Promise<Blob> => {
  const token = localStorage.getItem('token');
  const encodedPath = encodeURIComponent(filePath);
  const res = await fetch(`https://localhost:8443/api/contracts/files/preview?path=${encodedPath}`, { // URL 형식을 ?path= 로 변경
    method: 'GET',
    credentials: 'include',
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  
  if (!res.ok) {
    throw new Error(`파일 스트리밍 실패: ${res.statusText}`);
  }
  
  return res.blob();
};

// 계약서 삭제
export const deleteContract = async (contractId: number) => {
  const token = localStorage.getItem('token');
  const res = await fetch(`https://localhost:8443/api/contracts/${contractId}`, {
    method: 'DELETE',
    credentials: 'include',
    headers: {
      ...defaultHeaders(),
      Authorization: `Bearer ${token}`,
    },
  });
  if (!res.ok) throw new Error(`계약서 삭제 실패: ${res.statusText}`);
  return res.json();
};

// 계약서 수정
export const updateContract = async (contractId: number, data: FormData) => {
  const token = localStorage.getItem('token');
  const res = await fetch(`https://localhost:8443/api/contracts/${contractId}`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      Authorization: `Bearer ${token}`,
      // Content-Type은 FormData 사용시 자동으로 설정되므로 제외
    },
    body: data,
  });
  if (!res.ok) throw new Error(`계약서 수정 실패: ${res.statusText}`);
  return res.json();
};

// 계약서 무결성 검증
export const verifyContractIntegrity = async (contractId: number, versionNumber: number) => {
  const token = localStorage.getItem('token');
  const res = await fetch(`https://localhost:8443/api/contracts/${contractId}/versions/${versionNumber}/verify`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      ...defaultHeaders(),
      Authorization: `Bearer ${token}`,
    },
  });
  if (!res.ok) throw new Error(`무결성 검증 실패: ${res.statusText}`);
  return res.json();
};

// 파일 다운로드용 - Fetch 방식으로 변경
export const downloadContractFileDirectly = async (filePath: string, fileName?: string): Promise<void> => {
  try {
    const blob = await downloadContractFile(filePath);
    
    // Blob URL 생성 및 다운로드
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    if (fileName) {
      link.download = fileName;
    }
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    // Blob URL 해제
    URL.revokeObjectURL(url);
  } catch (err) {
    console.error('파일 다운로드 오류:', err);
    throw err;
  }
};

// Blob 방식의 다운로드 (Authorization 헤더 사용)
export const downloadContractFile = async (filePath: string): Promise<Blob> => {
  const token = localStorage.getItem('token');
  // --- 수정된 부분 ---
  const encodedPath = encodeURIComponent(filePath);
  const res = await fetch(`https://localhost:8443/api/contracts/files/download?path=${encodedPath}`, { // URL 형식을 ?path= 로 변경
  // ------------------
    method: 'GET',
    credentials: 'include',
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  
  if (!res.ok) {
    throw new Error(`파일 다운로드 실패: ${res.statusText}`);
  }
  
  return res.blob();
};


