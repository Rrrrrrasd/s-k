import styled from 'styled-components'

interface IPIContainer {
  inFocus?: boolean
  isDropdown?: boolean
  isClickable?: boolean
}

export const PIContainer = styled.div<IPIContainer>`
  margin: 2px 0px;
  padding: 4px 8px;
  position: relative;

  display: flex;
  align-items: center;
  flex-direction: row;

  border: 0;
  border-radius: 8px;

  cursor: ${({ isClickable }) => (isClickable ? 'pointer' : 'default')};
  outline: none;
  overflow: hidden;
  user-select: none;
  background-color: ${({ inFocus, theme }) =>
    inFocus ? theme.colors.primary100 : 'transparent'};

  &:hover,
  &:focus-visible {
    background-color: ${({ inFocus, theme }) =>
      inFocus ? theme.colors.primary100 : 'rgba(60, 64, 67, 0.08)'};
  }

  & > span {
    padding-right: ${({ isDropdown }) => (isDropdown ? 20 : 0)}px;

    color: ${({ inFocus, theme }) =>
      inFocus ? theme.colors.primary : theme.colors.text};

    font: 400 1.125rem/1.5rem 'Roboto', Helvetica, Arial, sans-serif;
    font-variant-ligatures: no-contextual;

    overflow: hidden;
    text-overflow: ellipsis;
  }

  & > svg {
    color: ${({ theme }) => theme.colors.grey};
    width: 20px;
    height: 20px;
    opacity: 0.65;

    top: 6px;
    right: 3px;
    position: absolute;
  }
`
