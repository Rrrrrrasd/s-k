import styled from 'styled-components'

export const LTContainer = styled.button`
  width: 100%;
  height: 54px;
  padding: 0px 16px;

  display: flex;
  align-items: center;
  flex-direction: row;

  transition: background-color 0.1s ease-in-out;

  color: #000;
  font: normal 0.875rem/1.5 'Roboto', sans-serif;

  cursor: pointer;
  border: none;
  outline: none;
  background-color: transparent;

  & > p {
    color: inherit;
    font-weight: inherit;
    font: inherit;
  }

  &:hover,
  &:focus-visible {
    background-color: #eeeeee;
  }
`

export const LTIconContainer = styled.div`
  width: 24px;
  margin-right: 8px;

  display: flex;
  align-items: center;
  justify-content: center;

  & > img,
  & > svg {
    width: 16px;
    height: 16px;
  }
`

export const LTTextContent = styled.div`
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-start;

  font: inherit;
  color: inherit;
  font-weight: inherit;

  & > p:first-of-type {
    font: inherit;
    color: inherit;
    font-weight: inherit;
  }
`

export const LTSubtitle = styled.p`
  font: 400 0.75rem/1.25 'Roboto', sans-serif;
  color: ${({ theme }) => theme.colors.grey};
`

export const LTTrailingContainer = styled.div`
  margin-left: 8px;
  padding-bottom: 14px;

  display: flex;
  align-items: flex-end;
  flex-direction: column;

  & > span {
    color: #000;
    font: normal 0.75rem/1.5 'Roboto', sans-serif;
  }
`
// 메뉴 버튼과 드롭다운을 감싸는 컨테이너
export const MenuWrapper = styled.div`
  position: relative; // 드롭다운 메뉴의 기준점
  margin-left: 8px; // 오른쪽 요소들과의 간격
  display: flex; // 버튼 정렬 위해 추가
  align-items: center; // 버튼 세로 중앙 정렬 위해 추가
`;

// 삼단바(점 3개) 모양 버튼
export const MenuButton = styled.button`
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px; // 클릭 영역 확보
  margin: 0; // 기본 마진 제거
  line-height: 1; // 아이콘 정렬
  border-radius: 50%; // 원형 배경 효과 위해
  display: flex; // 아이콘 중앙 정렬 위해
  align-items: center;
  justify-content: center;
  color: #5f6368; // 아이콘 색상 (Google Drive 스타일 참조)

  &:hover {
    background-color: rgba(0, 0, 0, 0.08); // 연한 회색 배경
  }

  // 아이콘 (텍스트 사용 시)
  font-size: 20px;
  font-weight: bold;
  letter-spacing: -1px; // 점 간격 조절
`;

// 클릭 시 나타나는 드롭다운 메뉴
export const DropdownMenu = styled.div`
  position: absolute;
  top: calc(100% + 4px); // 버튼 바로 아래 + 약간의 간격
  right: 0; // 오른쪽에 붙임
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.12), 0 1px 2px rgba(0,0,0,0.24); // 그림자 효과
  padding: 8px 0; // 위아래 여백
  min-width: 120px; // 최소 너비
  z-index: 100; // 다른 요소들 위에 표시
  display: flex;
  flex-direction: column;
`;

// 드롭다운 메뉴 안의 각 항목 (즐겨찾기, 삭제하기)
export const MenuItem = styled.button<{ isDelete?: boolean }>` // 삭제 옵션 구분 위해 isDelete prop 추가
  background: none;
  border: none;
  text-align: left;
  padding: 8px 16px; // 내부 여백
  font-size: 14px;
  color: ${props => props.isDelete ? '#d93025' : '#202124'}; // 삭제는 빨간색, 기본은 어두운 회색
  cursor: pointer;
  display: flex; // 아이콘 추가 시 정렬 용이
  align-items: center; // 아이콘 추가 시 세로 중앙 정렬

  &:hover {
    background-color: #f1f3f4; // 호버 시 배경색
  }

  /* // 아이콘 사용 시 (예시)
  svg {
    margin-right: 12px;
    color: ${props => props.isDelete ? '#d93025' : '#5f6368'};
  } */
`;
