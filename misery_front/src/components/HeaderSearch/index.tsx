import { useRef, useState, useCallback } from 'react'
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

  const clearSearch = useCallback(() => {
    inputRef.current?.focus()
    setValue('')
  }, [])

  const onChange = useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
    setValue(event.target.value)
  }, [])

  const onFocus = useCallback(() => setOpenResults(true), [])
  
  const onBlur = useCallback((event: React.FocusEvent) => {
    // 검색 결과 내부 클릭 시에는 닫히지 않도록 처리
    const relatedTarget = event.relatedTarget as HTMLElement
    if (relatedTarget && relatedTarget.closest('[data-search-results]')) {
      return
    }
    // 약간의 지연을 두어 클릭 이벤트가 처리될 시간을 줌
    setTimeout(() => setOpenResults(false), 150)
  }, [])

  const handleKeyDown = useCallback((event: React.KeyboardEvent) => {
    if (event.key === 'Escape') {
      setOpenResults(false)
      inputRef.current?.blur()
    }
  }, [])

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
            placeholder="계약서 및 파일 검색"
            onChange={onChange}
            onFocus={onFocus}
            onBlur={onBlur}
            onKeyDown={handleKeyDown}
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

          {openResults && (
            <div data-search-results>
              <HeaderSearchResults value={value} />
            </div>
          )}
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