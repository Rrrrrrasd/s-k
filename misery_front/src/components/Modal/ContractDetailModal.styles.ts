import styled from 'styled-components';
import { colors } from './Modal'; // Ensure Modal.ts exists and exports colors

// 전체 모달 바디를 감싸는 Flex 컨테이너
export const ModalBodyWrapper = styled.div`
  display: flex;
  flex-direction: row;
  height: calc(80vh - 120px); // 예시 높이: 헤더/푸터 높이를 대략적으로 제외
  min-height: 400px; 
  padding: 0;
`;

// 왼쪽 PDF 미리보기 영역
export const PdfPreviewContainer = styled.div`
  flex: 1; 
  min-width: 300px; 
  border-right: 1px solid ${colors.secondary};
  padding: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden; 
`;

export const PdfFrame = styled.iframe`
  width: 100%;
  height: 100%;
  border: none;
`;

export const PdfMessage = styled.div`
  text-align: center;
  padding: 20px;
  color: ${colors.textSecondary};
  font-size: 0.9rem;
`;

// 오른쪽 계약 상세 정보 영역
export const DetailsContainer = styled.div`
  flex: 1; 
  min-width: 300px; 
  padding: 16px 24px;
  overflow-y: auto; 

  &::-webkit-scrollbar {
    width: 12px;
  }
  &::-webkit-scrollbar-thumb {
    background-color: rgba(0, 0, 0, 0.2);
    border-radius: 6px;
    border: 3px solid transparent;
    background-clip: content-box;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }
`;

export const ActionButtonGroup = styled.div`
  display: flex;
  gap: 12px;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
`;

interface ActionButtonProps {
  variant?: 'primary' | 'success' | 'warning' | 'danger' | 'info';
}
export const ActionButton = styled.button<ActionButtonProps>`
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: background-color 0.2s ease, opacity 0.2s ease;

  background-color: ${colors.secondary};
  color: ${colors.textPrimary};

  &:hover {
    opacity: 0.85;
  }

  ${({ variant }) => variant === 'primary' && `
    background-color: ${colors.primary};
    color: white;
  `}
  ${({ variant }) => variant === 'success' && `
    background-color: #4caf50;
    color: white;
  `}
  ${({ variant }) => variant === 'warning' && `
    background-color: #ff9800;
    color: white;
  `}
   ${({ variant }) => variant === 'danger' && `
    background-color: #d32f2f;
    color: white;
  `}
   ${({ variant }) => variant === 'info' && `
    background-color: #2196f3;
    color: white;
  `}

  &:disabled {
    background-color: #ccc;
    color: #888;
    cursor: not-allowed;
    opacity: 0.7;
  }

  svg {
    width: 16px;
    height: 16px;
  }
`;

export const InfoSection = styled.div`
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid ${colors.secondary};

  &:last-child {
    border-bottom: none;
    margin-bottom: 0;
    padding-bottom: 0;
  }
`;

export const InfoSectionTitle = styled.h3`
  font-size: 1.15rem;
  font-weight: 600;
  color: ${colors.textPrimary};
  margin-bottom: 0.75rem;
  display: flex;
  align-items: center;
  gap: 8px;

  svg { 
    width: 20px;
    height: 20px;
  }
`;

export const InfoItem = styled.div`
  margin-bottom: 0.6rem;
  font-size: 0.9rem;
  line-height: 1.5;
  color: ${colors.textSecondary};

  strong {
    color: ${colors.textPrimary};
    font-weight: 500; 
    margin-right: 8px;
    display: inline-block;
    min-width: 70px; 
  }
`;

export const StatusBadge = styled.span<{ statusColor?: string }>`
  display: inline-block;
  padding: 3px 8px;
  font-size: 0.75rem;
  font-weight: 500;
  border-radius: 12px;
  color: white;
  background-color: ${({ statusColor, theme }) => statusColor || theme.colors.grey};
  margin-left: 8px;
`;

export const FileHashText = styled.span`
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, Courier, monospace;
  font-size: 0.8rem;
  background-color: #f5f5f5;
  padding: 3px 6px;
  border-radius: 3px;
  margin-left: 8px;
  word-break: break-all;
`;

export const VerificationResultWrapper = styled(InfoSection)``;

export const VerificationTitleContainer = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.75rem;
`;

export const CloseVerificationButton = styled.button`
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1.2rem;
  color: ${colors.textSecondary};
  padding: 0;
  line-height: 1;

  &:hover {
    color: ${colors.textPrimary};
  }
`;

interface VerificationBoxProps {
  isSuccess?: boolean;
  status?: string; 
}

const getStatusColors = (status?: string) => {
  switch (status) {
    case 'SUCCESS': return { background: '#e8f5e8', border: '#4caf50', text: '#2e7d32' };
    case 'FAILED': return { background: '#ffebee', border: '#f44336', text: '#d32f2f' };
    case 'DATA_NOT_FOUND': return { background: '#fff3e0', border: '#ff9800', text: '#f57c00' };
    case 'ERROR': return { background: '#ffebee', border: '#f44336', text: '#d32f2f' };
    case 'NOT_CHECKED': return { background: '#f5f5f5', border: '#9e9e9e', text: '#666' };
    default: return { background: '#f5f5f5', border: '#e0e0e0', text: '#333' };
  }
};

export const OverallVerificationBox = styled.div<VerificationBoxProps>`
  padding: 12px 16px;
  border-radius: 4px;
  margin-bottom: 1rem;
  border: 1px solid ${({ isSuccess }) => (isSuccess ? getStatusColors('SUCCESS').border : getStatusColors('FAILED').border)};
  background-color: ${({ isSuccess }) => (isSuccess ? getStatusColors('SUCCESS').background : getStatusColors('FAILED').background)};
  color: ${({ isSuccess }) => (isSuccess ? getStatusColors('SUCCESS').text : getStatusColors('FAILED').text)};

  strong {
    font-weight: 600;
  }
  span {
    font-size: 0.875rem;
    display: block;
    margin-top: 4px;
  }
  .verified-time {
    font-size: 0.75rem;
    color: ${colors.textSecondary};
    margin-top: 8px;
  }
`;

export const VerificationStepDetailBox = styled.div<VerificationBoxProps>`
  padding: 10px 14px;
  border-radius: 4px;
  font-size: 0.875rem;
  margin-bottom: 0.75rem;
  border: 1px solid ${({ status }) => getStatusColors(status).border};
  background-color: ${({ status }) => getStatusColors(status).background};
  color: ${({ status }) => getStatusColors(status).text};

  h4 {
    font-size: 1rem;
    font-weight: 600;
    margin: 0 0 0.5rem 0;
    color: ${({ status }) => getStatusColors(status).text};
  }

  strong { 
    font-weight: 500;
  }
  
  .details-text {
    margin-top: 4px;
    line-height: 1.4;
  }
`;

export const DiscrepancyList = styled.ul`
  margin: 8px 0 4px 0;
  padding-left: 20px;
  list-style-type: disc;
`;

export const DiscrepancyItem = styled.li`
  font-size: 0.8rem;
  line-height: 1.4;
  margin-bottom: 4px;
`;

export const SigningStatusBox = styled.div<{
  backgroundColor?: string;
  borderColor?: string;
  textColor?: string;
}>`
  padding: 12px 16px;
  border-radius: 4px;
  margin-top: 1rem;
  border: 1px solid ${({ borderColor }) => borderColor || colors.secondary};
  background-color: ${({ backgroundColor }) => backgroundColor || '#f5f5f5'};
  color: ${({ textColor }) => textColor || colors.textPrimary};

  strong {
    font-weight: 600;
    display: block;
    margin-bottom: 4px;
  }
`;