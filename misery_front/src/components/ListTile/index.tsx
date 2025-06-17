import { ButtonHTMLAttributes, memo, ReactNode } from 'react'
import {
  LTContainer,
  LTIconContainer,
  LTSubtitle,
  LTTextContent,
  LTTrailingContainer,
} from './styles'

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement>
interface IProps {
  title: string
  subtitle?: string
  icon?: ReactNode
  trailing?: string
  iconStyle?: React.CSSProperties
  subtitleStyle?: React.CSSProperties
}

const ListTile = memo(function ListTile(props: IProps & ButtonProps) {
  return (
    <LTContainer {...props}>
      {props.icon && (
        <LTIconContainer style={props.iconStyle}>{props.icon}</LTIconContainer>
      )}

      <LTTextContent>
        <p>{props.title}</p>

        {props.subtitle && (
          <LTSubtitle style={props.subtitleStyle}>{props.subtitle}</LTSubtitle>
        )}
      </LTTextContent>

      {props.trailing && (
        <LTTrailingContainer>
          <span>{props.trailing}</span>
        </LTTrailingContainer>
      )}
    </LTContainer>
  )
})

export default ListTile
