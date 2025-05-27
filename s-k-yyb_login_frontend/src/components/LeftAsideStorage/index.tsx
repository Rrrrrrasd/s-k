import { memo } from 'react'
import { LASBuyButton, LASContainer, LASHorizontalIndicator } from './styles'

const LeftAsideStorage = memo(function LeftAsideStorage() {
  return (
    <LASContainer>
      <div>
        <LASHorizontalIndicator>
          <div></div>
        </LASHorizontalIndicator>

        <a aria-label="Storage Summary: 7.17 GB of 15 GB used" data-no-tooltip>
          7.17 GB of 15 GB used
        </a>

        <LASBuyButton aria-label="Buy storage" data-no-tooltip>
          Buy storage
        </LASBuyButton>
      </div>
    </LASContainer>
  )
})

export default LeftAsideStorage
