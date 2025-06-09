// src/Modal/styles.ts (가정된 경로)
import styled from 'styled-components';
import { colors } from './Modal'; // 같은 폴더 내 Modal.ts 에서 colors 가져오기

// --- 모달 내부 요소 스타일 컴포넌트 ---

export const ModalHeader = styled.div`
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 1.5rem 1.5rem 1rem;
`;

export const ModalLogo = styled.div`
  /* 로고 컨테이너 스타일 (필요시) */
`;

export const LogoCircle = styled.span`
  width: 3.5rem;
  height: 3.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: ${colors.primaryAccent};

  svg {
    max-width: 1.5rem;
    fill: ${colors.primary};
  }
`;

export const CloseButton = styled.button`
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2.25rem;
  height: 2.25rem;
  border-radius: 0.25rem;
  border: none;
  cursor: pointer;
  background-color: transparent;
  padding: 0;

  svg {
    width: 24px;
    height: 24px;
    fill: ${colors.textSecondary}; // 닫기 버튼 색상 지정
  }

  &:hover,
  &:focus {
    background-color: ${colors.primaryAccent};
  }
`;

export const ModalBody = styled.div`
  padding: 1rem 1.5rem;
`;

export const ModalTitle = styled.h2`
  margin: 0 0 0.5rem 0;
  font-size: 1.5rem;
  color: ${colors.textPrimary};
  font-weight: bold;
`;

export const ModalDesc = styled.p`
  color: ${colors.textSecondary};
  margin: 0 0 1rem 0;
`;

export const UploadArea = styled.button`
  margin-top: 1.25rem;
  border: 4px dashed ${colors.secondary};
  background-color: transparent;
  padding: 3rem;
  width: 100%;
  display: flex;
  align-items: center;
  flex-direction: column;
  cursor: pointer;
  border-radius: 0.25rem;
  text-align: center;

  &:hover,
  &:focus {
    border-color: ${colors.primary};
  }
`;

export const UploadIcon = styled.span`
  display: block;
  svg {
    width: 40px;
    height: 40px;
    fill: ${colors.primary};
  }
`;

export const UploadTitle = styled.span`
  margin: 1rem 0;
  display: block;
  font-weight: bold;
  color: ${colors.textPrimary};
`;

export const UploadDesc = styled.span`
  color: ${colors.textSecondary};
  strong {
    color: ${colors.primary};
    font-weight: bold;
  }
`;

export const ModalFooter = styled.div`
  padding: 1rem 1.5rem 1.5rem;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid ${colors.secondary};
  margin-top: 1rem;
`;

export const FooterButton = styled.button`
  margin-left: 0.75rem;
  padding: 0.6rem 1.2rem;
  border: 2px solid ${colors.secondary};
  border-radius: 0.25rem;
  background-color: transparent;
  cursor: pointer;
  font-weight: bold;
  color: ${colors.textSecondary};

  &:last-child {
    color: #fff;
    background-color: ${colors.primary};
    border-color: ${colors.primary};
  }

  &:hover:not(:last-child) {
    border-color: ${colors.textSecondary};
  }

  &:hover:last-child {
     opacity: 0.9;
  }
`;

export const InfoDisplay = styled.div`
  margin-bottom: 1rem;
  font-size: 0.9rem;
  color: ${colors.textSecondary};
  strong {
    color: ${colors.textPrimary};
    font-weight: bold;
    margin-right: 8px;
  }
`;

export const InputGroup = styled.div`
  margin-bottom: 1.5rem;
`;

export const StyledLabel = styled.label`
  display: block;
  margin-bottom: 0.5rem;
  font-weight: bold;
  color: ${colors.textPrimary};
  font-size: 0.9rem;
`;

export const StyledInput = styled.input`
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1px solid ${colors.secondary};
  border-radius: 0.25rem;
  box-sizing: border-box;
  font-size: 1rem;
  &:focus {
    border-color: ${colors.primary};
    outline: none;
    box-shadow: 0 0 0 2px ${colors.primaryAccent};
  }
  &::placeholder {
    color: #aaa;
  }
`;