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
