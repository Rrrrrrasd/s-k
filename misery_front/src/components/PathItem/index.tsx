import { memo } from 'react'
import { ReactComponent as DownIcon } from '../../assets/icons/chevron-down.svg'
import { PIContainer } from './styles'

interface IProps {
  text: string
  inFocus?: boolean
  isDropdown?: boolean
  onClick?: () => void
}

const PathItem = memo(function PathItem(props: IProps) {
  return (
    <PIContainer {...props} isClickable={!!props.onClick}>
      <span>{props.text}</span>
      {props.isDropdown && <DownIcon />}
    </PIContainer>
  )
})

export default PathItem
