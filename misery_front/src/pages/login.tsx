import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { LoginContainer, LoginForm, FormTitle, FormDiv, FormInput, FormLabel, FormButton, ButtonContainer } from '../components/login/styles'; // 가정: styles.ts 파일이 해당 경로에 있습니다.
import { loginApi } from '../utils/api';
import { FormLinkText } from '../styles';

const Login: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const res = await loginApi(email, password);
      if (res.success) {
        const { accessToken, refreshToken } = res.data;
        localStorage.setItem('token', accessToken); // JWT 저장
        localStorage.setItem('refreshToken', refreshToken);
        navigate('/app'); // 성공 시 메인 페이지로
      } else {
        alert(res.message || '로그인 실패');
      }
    } catch (err) {
      alert('서버 오류');
    }
  };

  const handleSignupClick = () => {
    navigate('/signup'); // "/signup" 경로로 이동합니다.
  };
  const handleWebAuthnClick = () => {
    navigate('/webauthn/login');
  };

  return (
    <LoginContainer>
      <LoginForm onSubmit={handleLogin}>
        <FormTitle>로그인</FormTitle>

        <FormDiv>
          <FormInput
            type="text"
            placeholder="이메일"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </FormDiv>

        <FormDiv>
          <FormInput
            type="password"
            placeholder="비밀번호"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </FormDiv>

        <ButtonContainer>
          <FormButton type="button" value="회원가입" onClick={handleSignupClick} />
          <FormButton type="submit" value="로그인" />
        </ButtonContainer>
        <FormLinkText onClick={handleWebAuthnClick}>
          Passkey로 로그인하기
        </FormLinkText>
      </LoginForm>
    </LoginContainer>
  );
};

export default Login;