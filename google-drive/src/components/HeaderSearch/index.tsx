import { useRef, useState } from 'react'
import { ReactComponent as AskIcon } from '../../assets/icons/ask.svg'
import { ReactComponent as CloseIcon } from '../../assets/icons/close.svg'
import { ReactComponent as GearIcon } from '../../assets/icons/gear.svg'
import { ReactComponent as SearchIcon } from '../../assets/icons/search.svg'
import { ReactComponent as SettingsIcon } from '../../assets/icons/settings.svg'
import HeaderSearchResults from '../HeaderSearchResults'
import { DropButton, HSContainer, HSForm, HSFormContainer } from './styles'

function HeaderSearch() {
  const inputRef = useRef<HTMLInputElement>(null)

  const [value, setValue] = useState('')
  const [openResults, setOpenResults] = useState(false)

  const clearSearch = () => {
    inputRef.current?.focus()
    setValue('')
  }

  const onChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setValue(event.target.value)
  }

  const onFocus = () => setOpenResults(true)
  const onBlur = () => setOpenResults(false)

  return (
    <HSContainer>
      <HSFormContainer>
        <HSForm openResults={openResults}>
          <button type="button" aria-label="Search">
            <SearchIcon />
          </button>

          <input
            ref={inputRef}
            type="text"
            value={value}
            placeholder="Search in Drive"
            onChange={onChange}
            onFocus={onFocus}
            onBlur={onBlur}
          />

          <button
            type="button"
            aria-label="Clear search"
            disabled={!value}
            onClick={clearSearch}
          >
            <CloseIcon />
          </button>

          <button type="button" aria-label="Search options">
            <SettingsIcon />
          </button>

          {openResults && <HeaderSearchResults value={value} />}
        </HSForm>
      </HSFormContainer>

      <DropButton type="button" aria-label="Support">
        <AskIcon />
      </DropButton>

      <DropButton type="button" darkHover aria-label="Settings">
        <GearIcon />
      </DropButton>
    </HSContainer>
  )
}

export default HeaderSearch
