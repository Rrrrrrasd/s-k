import styled from 'styled-components'

export const LASContainer = styled.div`
  padding-top: 8px;
  margin-left: 27px;
  margin-right: 16px;

  user-select: none;

  & > div {
    max-width: 175px;

    display: flex;
    flex-direction: column;

    & > a:first-of-type {
      margin-bottom: 7px;
      outline: none;
      cursor: pointer;

      color: ${({ theme }) => theme.colors.grey};
      font: 400 0.813rem/0.938rem 'Roboto', Helvetica, Arial, sans-serif;
    }
  }
`

export const LASHorizontalIndicator = styled.div`
  height: 4px;
  margin-bottom: 8px;

  display: flex;
  flex-flow: row;

  overflow: hidden;
  border-radius: 3px;
  background-color: rgba(0, 0, 0, 0.12);

  & > div {
    width: 50%;
    height: 100%;

    box-shadow: 2px 0 0 #fff;
    -webkit-box-shadow: 2px 0 0 #fff;

    border-radius: 3px;
    background-color: ${({ theme }) => theme.colors.primaryIndicator};
  }
`

export const LASBuyButton = styled.a`
  height: 36px;
  margin: 6px 0px;
  padding: 0 23px 0 23px;
  align-self: flex-start;

  color: ${({ theme }) => theme.colors.primary};
  font: 500 0.875rem/1.063rem 'Roboto', Helvetica, Arial, sans-serif;
  letter-spacing: 0.0107142857em;

  display: flex;
  align-items: center;
  justify-content: center;

  border: 1px solid #dadce0;
  border-radius: 4px;

  cursor: pointer;
  outline: none;
  background-color: transparent;

  &:hover,
  &:focus-visible {
    color: #185abc;
    background-color: #f6f9fd;
  }
`
