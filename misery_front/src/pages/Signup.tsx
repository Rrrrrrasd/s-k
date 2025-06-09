import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { LoginContainer, LoginForm, FormTitle, FormDiv, FormInput, FormLabel, FormButton } from '../components/login/styles'; // Login 컴포넌트의 스타일 재사용
import { signupApi } from '../utils/api';

const Signup: React.FC = () => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const navigate = useNavigate();

  const handleSignUp = async (e: React.FormEvent) => {
    e.preventDefault();
    if (password !== confirmPassword) {
      alert('비밀번호 불일치');
      return;
    }
  
    try {
      const res = await signupApi(name, email, password);
      if (res.success) {
        alert('회원가입 성공!');
        navigate('/login');
      } else {
        alert(res.message || '회원가입 실패');
      }
    } catch {
      alert('서버 오류');
    }
  };

  return (
    <LoginContainer>
      <LoginForm onSubmit={handleSignUp}>
        <FormTitle>회원가입</FormTitle>

        <FormDiv>
          <FormInput
            type="text"
            placeholder="이름"
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
        </FormDiv>

        <FormDiv>
          <FormInput
            type="email"
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

        <FormDiv>
          <FormInput
            type="password"
            placeholder="비밀번호 확인"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
          />
        </FormDiv>

        <FormButton type="submit" value="회원가입" />
      </LoginForm>
    </LoginContainer>
  );
};

export default Signup;