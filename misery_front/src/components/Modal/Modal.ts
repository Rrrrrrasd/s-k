// src/Modal/Modal.ts (가정된 경로)
import ReactModal from 'react-modal';

// 색상 정의 및 내보내기
export const colors = {
  primary: '#2e44ff',
  primaryAccent: '#e9e5ff',
  textPrimary: '#0d0f21',
  textSecondary: '#6a6b76',
  secondary: '#e5e5e5',
};

const CustomModal: ReactModal.Styles = {
  overlay: {
    backgroundColor: "rgba(0, 0, 0, 0.4)",
    width: "100%",
    height: "100vh",
    zIndex: 1000,
    position: "fixed",
    top: "0",
    left: "0",
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  content: {
    width: "98%",
    maxWidth: "500px",
    backgroundColor: "#fff",
    borderRadius: "0.5rem",
    boxShadow: "0 5px 15px rgba(0, 0, 0, 0.2)",
    position: "relative",
    top: "auto",
    left: "auto",
    right: "auto",
    bottom: "auto",
    zIndex: 1050,
    transform: "none",
    overflow: "auto",
    WebkitOverflowScrolling: 'touch',
    outline: 'none',
    padding: "0", // 내부 컴포넌트가 패딩 처리
  },
};

export default CustomModal;