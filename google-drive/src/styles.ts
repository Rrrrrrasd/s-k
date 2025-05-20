import styled from 'styled-components'

export const AContainer = styled.div`
  width: 100%;
  height: 100vh;
  overflow-x: hidden;

  display: grid;
  grid-template-rows: auto 1fr;
  grid-template-columns: auto 1fr auto;
  grid-template-areas:
    'head head head'
    'lnav main main';
`

export const AMain = styled.section`
  grid-area: 'main';

  display: flex;
  flex-direction: column;
`

export const AContent = styled.div`
  &::-webkit-scrollbar {
    width: 16px;
    height: 16px;
    overflow: visible;
  }

  &::-webkit-scrollbar-button {
    width: 0;
    height: 0;
  }

  &::-webkit-scrollbar-corner {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    padding: 100px 0 0;
    min-height: 28px;

    border: solid transparent;
    border-width: 1px 1px 1px 6px;

    -webkit-box-shadow: inset 1px 1px 0 rgb(0 0 0 / 10%),
      inset 0 -1px 0 rgb(0 0 0 / 7%);
    box-shadow: inset 1px 1px 0 rgb(0 0 0 / 10%), inset 0 -1px 0 rgb(0 0 0 / 7%);

    background-clip: padding-box;
    background-color: rgba(0, 0, 0, 0.2);
  }

  &::-webkit-scrollbar-track {
    border: solid transparent;
    border-width: 0 0 0 4px;
    background-clip: padding-box;
  }
`

export const AContentTable = styled.table`
  width: 100%;
  padding-top: 8px;
  padding-right: 64px;

  -webkit-transition: padding-right 300ms cubic-bezier(0.4, 0, 0.2, 1);
  transition: padding-right 300ms cubic-bezier(0.4, 0, 0.2, 1);

  will-change: padding-right;

  & tr {
    display: flex;
    align-items: center;
    flex-direction: row;

    & > th,
    & > td {
      flex: 1;
      height: 40px;
      padding: 0px 6px;

      display: flex;
      align-items: center;
      flex-direction: row;
      justify-content: flex-start;

      transition: width 250ms ease-in-out, flex-basis 250ms ease-in-out;

      user-select: none;
      border-bottom: 1px solid #dadce0;

      &:nth-child(2) {
        flex: 0 0 215px;
      }

      &:nth-child(3) {
        flex: 0 0 200px;
      }

      &:nth-child(4) {
        flex: 0 0 140px;
      }

      @media (max-width: 1239px) {
        &:nth-child(2) {
          flex: 0 0 140px;
        }

        &:nth-child(3) {
          flex: 0 0 144px;
        }
      }

      @media (max-width: 1109px) {
        &:nth-child(4) {
          display: none;
        }
      }

      @media (max-width: 968px) {
        &:nth-child(2) {
          display: none;
        }
      }
    }

    & > th {
      font: 500 0.813rem 'Roboto', Helvetica, Arial, sans-serif;
      color: rgba(0, 0, 0, 0.54);
    }

    & > td {
      height: 48px;

      font: 400 0.813rem 'Roboto', Helvetica, Arial, sans-serif;
      color: rgba(0, 0, 0, 0.54);

      &:first-of-type {
        padding-left: 0px;
        overflow: hidden;

        font: 500 0.813rem 'Roboto', Helvetica, Arial, sans-serif;
        color: rgba(0, 0, 0, 0.72);

        & > div {
          width: 56px;
          height: 24px;
          max-width: 56px;

          display: flex;
          align-items: center;
          justify-content: center;

          & > img {
            width: 16px;
            height: 16px;
          }

          & > svg {
            width: 24px;
            height: 24px;
          }
        }

        & > span {
          flex: 1;
          color: rgba(0, 0, 0, 0.72);
          padding-left: 0px;
          padding-right: 8px;

          overflow: hidden;
          white-space: pre;
          text-overflow: ellipsis;
        }
      }

      & > span {
        color: ${({ theme }) => theme.colors.grey};
        padding-left: 5px;
      }
    }
  }
`
