// src/components/WebAuthnLogin.tsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  getWebAuthnLoginOptions,
  verifyWebAuthnLogin
} from '../utils/api';
import {
  preformatAssertionRequest,
  publicKeyCredentialToJSON
} from '../utils/webauthn';
import { ButtonContainer, FormButton, FormDiv, FormInput, FormTitle, LoginContainer, LoginForm } from '../components/login/styles';

const WebAuthnLogin: React.FC = () => {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      // 1) 백엔드에서 로그인 옵션 가져오기
      const options = await getWebAuthnLoginOptions(email);
      const assertionOptions = options.publicKeyCredentialRequestOptions;
      if (!assertionOptions) {
        throw new Error('로그인 옵션이 올바르지 않습니다.');
      }

      // 2) navigator.credentials.get에 넘길 옵션 가공
      const publicKey = preformatAssertionRequest({ ...assertionOptions });
      
      // 3) WebAuthn API 호출
      const credential = (await navigator.credentials.get({
        publicKey
      })) as PublicKeyCredential;
      
      // 4) 결과를 JSON으로 직렬화
      const credentialJSON = publicKeyCredentialToJSON(credential);

       // 5) 백엔드 검증 요청: 원본 options 전체를 request로 전송
        const payload = {
            request: options,
            response: credentialJSON
        };
      const result = await verifyWebAuthnLogin(payload);

      if (result.success) {
        // 6) 로그인 성공 처리 (토큰 저장 등)
        const { accessToken, refreshToken } = result.data;
        localStorage.setItem('token',        accessToken);
        localStorage.setItem('refreshToken', refreshToken);
        alert('WebAuthn 로그인 성공');
        navigate('/app');
      } else {
        alert(result.message || '로그인 실패');
      }
    } catch (err) {
      console.error(err);
      alert('WebAuthn 로그인 중 오류가 발생했습니다');
    } finally {
      setLoading(false);
    }
  };

  return (
    <LoginContainer>
        <LoginForm onSubmit={handleLogin}>
            <FormTitle>WebAuthn 로그인</FormTitle>

            <FormDiv>
                <FormInput
                type="email"
                placeholder="이메일"
                value={email}
                onChange={e => setEmail(e.target.value)}
                />
            </FormDiv>
            <ButtonContainer>
                <FormButton type="submit" value="Sign In" />
            </ButtonContainer>
        </LoginForm>
      
      
      
    </LoginContainer>
  );
};


export default WebAuthnLogin;
