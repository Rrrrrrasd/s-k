import { useState } from 'react'
import { ReactComponent as GridView } from '../../assets/icons/grid-view.svg'
import { ReactComponent as InfoCircle } from '../../assets/icons/info-circle.svg'
import PathItem from '../PathItem'
import { PBButton, PBContainer, PBContent } from './styles'

function PathBar() {
  const [showDropdown, setShowDropdown] = useState(false)

  return (
    <PBContainer>
      <PBContent>
        <PathItem
          text="My Drive"
          inFocus={showDropdown}
          isDropdown
          onClick={() => setShowDropdown(oldState => !oldState)}
        />

        <div>
          <PBButton type="button" aria-label="Grid view">
            <GridView />
          </PBButton>

          <PBButton
            type="button"
            aria-label="View details"
            data-tooltip-align="end"
          >
            <InfoCircle />
          </PBButton>
        </div>
      </PBContent>
    </PBContainer>
  )
}

export default PathBar
