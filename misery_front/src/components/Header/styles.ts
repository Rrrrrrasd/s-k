import styled from 'styled-components'

export const HContainer = styled.header`
  width: 100%;
  padding: 0.5rem;
  margin-top: -1px;

  grid-area: head;

  display: flex;
  align-items: center;

  border-bottom: 1px solid ${({ theme }) => theme.colors.divider};
`

export const HLogoContainer = styled.div`
  height: 48px;
  min-width: 238px;
  padding-right: 30px;

  display: flex;
  align-items: center;

  user-select: none;
  white-space: nowrap;
  vertical-align: middle;

  & > a {
    padding-left: 12px;

    display: flex;
    align-items: center;
    flex-direction: row;

    color: ${({ theme }) => theme.colors.grey};
    font: normal 1.375rem/1.5rem 'Noto Sans', Arial, sans-serif;
    letter-spacing: -0.063rem;

    top: 2px;
    position: relative;
    vertical-align: middle;

    cursor: pointer;
    outline: none;
    text-decoration: none;

    &:focus {
      text-decoration: underline;
    }

    & > img {
      width: 44px;
      height: 40px;
      margin-bottom: 4px;
      padding-right: 4px;
    }

    & > span {
      display: inline-block;
      padding-left: 4px;

      top: -1.5px;
      position: relative;
      vertical-align: middle;
    }
  }
`

export const HUser = styled.div`
  height: 48px;
  padding: 0px 4px;
  padding-left: 28px;

  display: flex;
  align-items: center;
  flex-direction: row;
`

export const HAppsButton = styled.div`
  padding: 4px 2px;

  & > div {
    width: 40px;
    height: 40px;

    & > button {
      width: 100%;
      height: 100%;
      padding: 8px;

      border: 0;
      cursor: pointer;
      outline: none;
      border-radius: 50%;
      background-color: transparent;

      &:hover,
      &:focus {
        background-color: rgba(60, 64, 67, 0.08);
      }

      & > svg {
        width: 24px;
        height: 24px;

        fill: currentColor;
        color: ${({ theme }) => theme.colors.grey};
      }
    }
  }
`

export const HUserButton = styled.div`
  padding: 4px 2px;
  padding-left: 6px;

  & > div {
    width: 40px;
    height: 40px;

    & > button {
      width: 100%;
      height: 100%;
      padding: 4px;

      border: 0;
      cursor: pointer;
      outline: none;
      user-select: none;
      border-radius: 50%;
      background-color: transparent;

      &:hover,
      &:focus {
        background-color: rgba(60, 64, 67, 0.08);
      }

      & > img {
        width: 32px;
        height: 32px;
        border-radius: 52%;
      }
    }
  }
`
