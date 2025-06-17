import styled from 'styled-components'

export const LABContainer = styled.button<{ isActive?: boolean }>`
  width: 100%;
  height: 40px;
  padding-left: 3px;

  display: flex;
  align-items: center;
  flex-direction: row;

  cursor: pointer;
  border: none;
  outline: none;
  user-select: none;
  white-space: nowrap;

  border-radius: 0px 66px 66px 0px;
  background-color: ${({ isActive, theme }) =>
    isActive ? theme.colors.primary100 : 'transparent'};

  &:hover {
    background-color: ${({ isActive, theme }) =>
      isActive ? theme.colors.primary100 : 'rgba(0, 0, 0, 0.04)'};
  }

  &[aria-selected='true'] {
    &:focus-visible {
      outline: #4d90fe solid 1px;
      background-color: transparent;
    }
  }

  & > div:first-of-type {
    width: 24px;
    padding: 0px 4px 0px 6px;
    position: relative;

    display: flex;
    align-items: center;
    justify-content: center;

    & > svg {
      color: #5f6368;
      position: absolute;
      cursor: auto;
    }
  }
`

export const LABContent = styled.div<{ isActive?: boolean }>`
  flex: 1;
  margin-left: -5px;
  padding-left: 5px;

  font: ${({ isActive }) => (isActive ? 600 : 500)} 0.813rem/1.625rem 'Roboto',
    Helvetica, Arial, sans-serif;

  color: ${({ isActive, theme }) =>
    isActive ? theme.colors.primary : '#5f6368'};

  overflow: hidden;

  display: flex;
  align-items: center;
  flex-direction: row;

  & > div {
    display: flex;
    align-items: center;
    padding-right: 20px;
    color: ${({ isActive, theme }) =>
      isActive ? theme.colors.primarySvg : 'inherit'};

    & > svg {
      width: 24px;
      height: 24px;
    }
  }
`
