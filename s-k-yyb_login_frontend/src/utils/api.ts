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




