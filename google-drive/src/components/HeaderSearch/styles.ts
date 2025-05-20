import styled from 'styled-components'

export const HSContainer = styled.div`
  flex: 1;

  display: flex;
  align-items: center;
  flex-direction: row;
  justify-content: space-between;
`

export const HSFormContainer = styled.div`
  width: 100%;
  height: 48px;
  padding-left: 10px;
  padding-right: 30px;
`

export const HSForm = styled.form<{ openResults: boolean }>`
  width: 100%;
  height: 100%;
  max-width: 722px;

  display: flex;
  flex-wrap: wrap;
  align-items: center;
  flex-direction: row;

  position: relative;
  transition: background-color 0.1s ease-in-out;

  border: 1px solid transparent;
  border-radius: 0.5rem;
  background-color: ${({ theme }) => theme.colors.onWhite};

  &:focus {
    outline: #4d90fe solid 1px;
  }

  & > input {
    flex: 1;
    z-index: 3;

    color: #000;
    font: normal 1rem 'Noto Sans', 'Roboto', Helvetica, Arial, sans-serif;
    letter-spacing: -0.016rem;

    border: 0;
    outline: none;
    background-color: transparent;

    &::placeholder {
      user-select: none;
    }
  }

  & > button {
    height: 100%;
    padding: 0px 5px;
    z-index: 3;

    border: none;
    cursor: pointer;
    outline: none;
    background: none;

    &:disabled {
      opacity: 0;
      cursor: default;
    }

    & > svg {
      width: 40px;
      height: 40px;

      margin: 3px;
      padding: 8px;

      fill: currentColor;
      color: ${({ theme }) => theme.colors.grey};
    }

    &:hover > svg,
    &:focus > svg {
      border-radius: 50%;
      background-color: rgba(60, 64, 67, 0.08);
    }
  }
`

export const DropButton = styled.button<{ darkHover?: boolean }>`
  width: 40px;
  height: 40px;
  min-width: 40px;
  min-height: 40px;
  margin: 2px;

  display: flex;
  align-items: center;
  justify-content: center;

  border: 1px solid transparent;
  border-radius: 50%;

  cursor: pointer;
  outline: none;
  background: none;

  & > svg {
    width: 24px;
    height: 24px;

    fill: currentColor;
    color: ${({ theme }) => theme.colors.grey};
  }

  &:hover,
  &:focus {
    background-color: ${({ darkHover }) =>
      darkHover ? 'rgba(0 ,0 , 0, 0.12)' : 'rgba(60, 64, 67, 0.08)'};
  }
`
