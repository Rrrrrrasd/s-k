import styled from 'styled-components'

export const PBContainer = styled.div`
  min-height: 48px;
  padding-left: 1px;
  padding-right: 12px;

  border-bottom: 1px solid ${({ theme }) => theme.colors.divider};
`

export const PBContent = styled.div`
  height: 48px;

  display: flex;
  align-items: center;
  flex-direction: row;
  justify-content: space-between;

  & > div {
    display: flex;
    align-items: center;
    flex-direction: row;
  }
`

export const PBButton = styled.button`
  width: 40px;
  height: 40px;
  margin: 0px 4px;

  display: flex;
  align-items: center;
  justify-content: center;

  transition: color 200ms cubic-bezier(0.4, 0, 0.2, 1),
    background 200ms cubic-bezier(0.4, 0, 0.2, 1),
    box-shadow 400ms cubic-bezier(0.4, 0, 0.2, 1);

  cursor: pointer;
  border: 0;
  outline: none;
  user-select: none;
  border-radius: 50%;
  background-color: transparent;

  & > svg {
    width: 24px;
    height: 24px;

    opacity: 0.54;
    fill: #000000;
  }

  &:hover,
  &:focus-visible {
    background: rgba(0, 0, 0, 0.12);

    & > svg {
      opacity: 1;
    }
  }
`
