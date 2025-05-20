import userImg from '../../assets/bighead.svg'
import { ReactComponent as GridAppsIcon } from '../../assets/icons/grid.svg'
import logoImg from '../../assets/logo.png'
import logo2Img from '../../assets/logo@2x.png'
import HeaderSearch from '../HeaderSearch'
import {
  HAppsButton,
  HContainer,
  HLogoContainer,
  HUser,
  HUserButton,
} from './styles'

function Header() {
  return (
    <HContainer>
      <HLogoContainer>
        <a href="/">
          <img src={logoImg} alt="" srcSet={`${logoImg} 1x, ${logo2Img} 2x`} />
          <span>Drive</span>
        </a>
      </HLogoContainer>

      <HeaderSearch />

      <HUser>
        <HAppsButton>
          <div>
            <button type="button" aria-label="Google apps">
              <GridAppsIcon />
            </button>
          </div>
        </HAppsButton>

        <HUserButton>
          <div>
            <button
              type="button"
              aria-label="Google Account: Development User (developmentUser@email.com)"
              data-tooltip-align="end"
            >
              <img src={userImg} alt="" />
            </button>
          </div>
        </HUserButton>
      </HUser>
    </HContainer>
  )
}

export default Header
