import styled from 'styled-components'

export const NBContainer = styled.div`
  padding: 13px 11px 12px;
  position: relative;

  & > button {
    height: 48px;
    min-width: 120px;
    min-height: 32px;
    padding-right: 24px;

    color: ${({ theme }) => theme.colors.grey100};
    font: 500 0.875rem/1.375rem 'Roboto', sans-serif;
    letter-spacing: 0.009rem;

    display: inline-flex;
    align-items: center;

    box-shadow: 0 1px 2px 0 rgb(60 64 67 / 30%),
      0 1px 3px 1px rgb(60 64 67 / 15%);

    transition: background-color 200ms cubic-bezier(0.4, 0, 0.2, 1),
      box-shadow 400ms cubic-bezier(0.4, 0, 0.2, 1);

    cursor: pointer;
    outline: none;
    overflow: hidden;
    user-select: none;

    border: 1px solid transparent;
    border-radius: 24px;

    background-color: #fff;

    &:hover,
    &:focus-visible {
      background-color: #f8f9fa;
      box-shadow: 0 1px 3px 0 rgb(60 64 67 / 30%),
        0 4px 8px 3px rgb(60 64 67 / 15%);
    }

    & > svg {
      margin: 0px 13px 0px 9px;
    }
  }
`

export const DropdownContainer = styled.div`
  position: absolute;
  top: 100%;
  left: 15px;
  background-color: white;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  z-index: 10; /* 다른 요소 위에 표시 */
`

export const DropdownItem = styled.div`
  padding: 10px 15px;
  cursor: pointer;
  &:hover {
    background-color: #f0f0f0;
  }
`
