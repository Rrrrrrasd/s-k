import React, { useState, useRef, useEffect } from 'react';
// 스타일 파일을 ListTile과 공유한다고 가정
import * as S from '../ListTile/styles';

interface ListTileMenuProps {
  fileId: string; // 어떤 항목의 메뉴인지 구분하기 위한 ID
  itemType?: 'file' | 'folder'; // 아이템 타입 prop (옵셔널)
}

const ListTileMenu: React.FC<ListTileMenuProps> = ({ fileId, itemType = 'file' }) => { // 기본값 'file'
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  // 메뉴 열고 닫는 로직
  const handleMenuToggle = (event: React.MouseEvent) => {
    event.stopPropagation(); // 이벤트 전파 중지
    setIsMenuOpen(prev => !prev);
  };

  // 즐겨찾기 클릭 핸들러
  const handleFavoriteClick = (event: React.MouseEvent) => {
    event.stopPropagation();
    console.log(`(즐겨찾기) ${itemType}: ${fileId}`);
    setIsMenuOpen(false);
    // TODO: 실제 즐겨찾기 API 호출 (파일에만 해당)
  };

  // 삭제 클릭 핸들러
  const handleDeleteClick = (event: React.MouseEvent) => {
    event.stopPropagation();
    console.log(`(삭제하기) ${itemType}: ${fileId}`);
    setIsMenuOpen(false);
    // TODO: 실제 삭제 확인 및 API 호출 (폴더/파일 공통)
  };

  // 외부 클릭 감지하여 닫기
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setIsMenuOpen(false);
      }
    };
    if (isMenuOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    } else {
      document.removeEventListener('mousedown', handleClickOutside);
    }
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isMenuOpen]);

  return (
    <S.MenuWrapper ref={menuRef}>
      <S.MenuButton onClick={handleMenuToggle}>
        ⋮
      </S.MenuButton>
      {isMenuOpen && (
        <S.DropdownMenu>
          {/* itemType이 'file'일 경우에만 즐겨찾기 표시 */}
          {itemType === 'file' && (
            <S.MenuItem onClick={handleFavoriteClick}>
              즐겨찾기
            </S.MenuItem>
          )}
          {/* 삭제하기는 항상 표시 */}
          <S.MenuItem onClick={handleDeleteClick} isDelete>
            삭제하기
          </S.MenuItem>
        </S.DropdownMenu>
      )}
    </S.MenuWrapper>
  );
};

export default ListTileMenu;