import { useNavigate } from 'react-router-dom'
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
import { logoutApi } from '../../utils/api'



function Header() {
  const navigate = useNavigate();
  
  const handleLogout = async () => {
    try {
      await logoutApi();
      // 로컬 토큰 삭제
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      navigate('/login');
    } catch (e) {
      console.error(e);
      alert('로그아웃에 실패했습니다');
    }
  };
  
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
        <HUserButton>
          <div>
          <button
            type="button"
            onClick={handleLogout}
            aria-label="Logout">로그아웃</button>
          </div>
        </HUserButton>
      </HUser>
      
      
    </HContainer>
  )
}

export default Header
