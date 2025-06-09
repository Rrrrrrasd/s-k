import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getWebAuthnRegisterOptions, verifyWebAuthnRegister } from '../utils/api';
import { makeCredentialRequestToJSON, preformatMakeCredReq, publicKeyCredentialToJSON } from '../utils/webauthn';
import { ButtonContainer, FormButton, FormDiv, FormInput, FormTitle, LoginContainer, LoginForm } from '../components/login/styles';

const WebAuthnRegister: React.FC = () => {
  const [deviceName, setDeviceName] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      // 1. 백엔드에서 등록 옵션 가져오기
      const optionsResponse = await getWebAuthnRegisterOptions();
      const publicKey = preformatMakeCredReq(optionsResponse);

      // 2. WebAuthn API 실행
      const credential: PublicKeyCredential = await navigator.credentials.create({ publicKey }) as PublicKeyCredential;

      // 3. 백엔드로 전송할 데이터 구성
      const credentialJSON = await publicKeyCredentialToJSON(credential);
      const requestJSON = makeCredentialRequestToJSON(optionsResponse);

      
      const payload = {
        request: requestJSON,
        response: credentialJSON,
        deviceName
      };

      // 4. 등록 요청 전송
      const result = await verifyWebAuthnRegister(payload);
      if (result.success) {
        alert('WebAuthn 등록 성공');
        navigate('/app');
      } else {
        alert(result.message || '등록 실패');
      }
    } catch (e) {
      console.error(e);
      alert('WebAuthn 등록 실패');
    } finally {
      setLoading(false);
    }
  };

  return (
    <LoginContainer>
      <LoginForm onSubmit={handleRegister}>
        <FormTitle>PassKey 등록</FormTitle>
        <FormDiv>
          <FormInput
            type="text"
            placeholder="PassKey 이름"
            value={deviceName}
            onChange={(e) => setDeviceName(e.target.value)}
          />
        </FormDiv>
        <ButtonContainer>
            <FormButton type="submit" value="PassKey 등록" />
        </ButtonContainer>
      </LoginForm>
    </LoginContainer>

  );
};

export default WebAuthnRegister;
