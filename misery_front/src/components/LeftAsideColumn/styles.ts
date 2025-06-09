import styled from 'styled-components'

export const LACContainer = styled.aside`
  width: 257px;

  grid-area: lnav;

  display: flex;
  flex-direction: column;
`

export const LACItemsContainer = styled.div`
  padding-right: 17px;

  display: flex;
  flex-direction: column;
`

export const LACDivider = styled.div`
  margin: 8px 0px;
  border-bottom: 1px solid ${({ theme }) => theme.colors.divider};
`
