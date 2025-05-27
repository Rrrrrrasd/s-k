// src/App.tsx
import React, { useState, useEffect } from 'react';
import moment from 'moment';
import { ReactComponent as FolderIcon } from './assets/icons/folder.svg';
import Header from './components/Header';
import LeftAsideColumn from './components/LeftAsideColumn';
import PathBar from './components/PathBar';
import Tooltip from './components/Tooltip';
import files from './data/files.json';
import folders from './data/folders.json';
import { AContainer, AContent, AContentTable, AMain, Button  } from './styles';
import getIcon from './utils/getIcon';
import { refreshTokenApi } from './utils/api';
import { useNavigate } from 'react-router-dom';
import { FormButton } from './components/login/styles';

function App() {
  const [remainingTime, setRemainingTime] = useState<string>('');
  const navigate = useNavigate();
  // JWT 페이로드에서 exp(만료시각) 꺼내기
  function parseJwt(token: string) {
    const base64Url = token.split('.')[1];
    const base64    = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const json      = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(json) as { exp: number };
  }

  // 1초마다 남은 유효시간 계산
  useEffect(() => {
    const updateRemaining = () => {
      const token = localStorage.getItem('token');
      if (!token) {
        setRemainingTime('토큰 없음');
        return;
      }
      try {
        const { exp } = parseJwt(token);
        const diff   = exp * 1000 - Date.now();
        if (diff <= 0) {
          setRemainingTime('만료됨');
        } else {
          const h = Math.floor(diff / 3600000);
          const m = Math.floor((diff % 3600000) / 60000);
          const s = Math.floor((diff % 60000) / 1000);
          setRemainingTime(`${m}분 ${s}초`);
        }
      } catch {
        setRemainingTime('유효하지 않음');
      }
    };

    updateRemaining();
    const timer = setInterval(updateRemaining, 1000);
    return () => clearInterval(timer);
  }, []);

  // 토큰 수동 갱신 핸들러
  const handleRefresh = async () => {
    try {
      const refreshToken = localStorage.getItem('refreshToken');
      if (!refreshToken) throw new Error('리프레시 토큰이 없습니다');
      const { data } = await refreshTokenApi();
      localStorage.setItem('token', data.accessToken);
      alert('토큰이 갱신되었습니다');
    } catch (e) {
      console.error(e);
      alert('토큰 갱신에 실패했습니다');
    }
  };

  const handleWebAuthnRegisterClick = () => {
    navigate('/webauthn/register'); 
  };

  return (
    <AContainer>
      <Header />
      <LeftAsideColumn />

      <AMain>
      
        {/* 토큰 갱신 버튼 & 남은 유효시간 */}
        <div style={{ display: 'flex', justifyContent: 'flex-end', padding: '8px 16px' }}>
          <Button onClick={handleWebAuthnRegisterClick}
            style={{ marginRight: 16 }}>PassKey등록</Button>
          <Button onClick={handleRefresh}>로그인 시간 갱신</Button>
          <span style={{ marginLeft: 12, lineHeight: '32px', fontSize: 14 }}>
            남은 시간: {remainingTime}
          </span>
        </div>

        <PathBar />

        <AContent>
          <AContentTable>
            <thead>
              <tr>
                <th>Name</th>
                <th>Owner</th>
                <th>Last modified</th>
                <th>File size</th>
              </tr>
            </thead>

            <tbody>
              {folders.map(folder => (
                <tr key={folder.id}>
                  <td>
                    <div><FolderIcon /></div>
                    <span>{folder.name}</span>
                  </td>
                  <td>me</td>
                  <td>{moment(folder.updatedAt).format('MMM DD, yyyy')}</td>
                  <td>-</td>
                </tr>
              ))}

              {files.map(file => (
                <tr key={file.id}>
                  <td>
                    <div>
                      <img src={getIcon(file.type)} alt="" />
                    </div>
                    <span>{file.name}</span>
                  </td>
                  <td>me</td>
                  <td>{moment(file.updatedAt).format('MMM DD, yyyy')}</td>
                  <td>
                    {file.size >= 1024
                      ? `${(file.size / 1024).toFixed(2)} MB`
                      : `${file.size} KB`}
                  </td>
                </tr>
              ))}
            </tbody>
          </AContentTable>
        </AContent>
      </AMain>

      <Tooltip />
    </AContainer>
  );
}

export default App;
