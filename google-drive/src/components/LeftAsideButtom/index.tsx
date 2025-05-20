import { memo, useMemo } from 'react'
import { ReactComponent as ChevronIcon } from '../../assets/icons/chevron-left.svg'
import { ReactComponent as ClockIcon } from '../../assets/icons/clock.svg'
import { ReactComponent as CloudIcon } from '../../assets/icons/cloud.svg'
import { ReactComponent as ComputersIcon } from '../../assets/icons/computers.svg'
import { ReactComponent as DriveIcon } from '../../assets/icons/drive.svg'
import { ReactComponent as GroupIcon } from '../../assets/icons/group.svg'
import { ReactComponent as StarIcon } from '../../assets/icons/star.svg'
import { ReactComponent as TrashIcon } from '../../assets/icons/trash.svg'
import { LABContainer, LABContent } from './styles'

interface IProps {
  type:
    | 'drive'
    | 'computers'
    | 'shared'
    | 'recent'
    | 'starred'
    | 'trash'
    | 'storage'
  isActive?: boolean
  showMore?: boolean
}

const LeftAsideButton = memo(function LeftAsideButton(props: IProps) {
  const Data = useMemo(() => {
    switch (props.type) {
      case 'drive':
        return { text: 'My Drive', Icon: DriveIcon }

      case 'computers':
        return { text: 'Computers', Icon: ComputersIcon }

      case 'shared':
        return { text: 'Shared with me', Icon: GroupIcon }

      case 'recent':
        return { text: 'Recent', Icon: ClockIcon }

      case 'starred':
        return { text: 'Starred', Icon: StarIcon }

      case 'trash':
        return { text: 'Trash', Icon: TrashIcon }

      case 'storage':
        return { text: 'Storage', Icon: CloudIcon }
    }
  }, [props.type])

  return (
    <LABContainer isActive={props.isActive} aria-selected={!!props.isActive}>
      <div>{props.showMore && <ChevronIcon />}</div>

      <LABContent isActive={props.isActive}>
        <div>{Data?.Icon && <Data.Icon />}</div>
        <span>{Data?.text}</span>
      </LABContent>
    </LABContainer>
  )
})

export default LeftAsideButton
