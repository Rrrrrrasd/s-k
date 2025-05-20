import { ReactNode, useEffect, useMemo } from 'react'
import ReactDOM from 'react-dom'

const modalRoot = document.getElementById('modal-root')

function Modal(props: { children?: ReactNode }) {
  const el = useMemo(() => document.createElement('div'), [])

  useEffect(() => {
    modalRoot?.appendChild(el)

    return () => {
      modalRoot?.removeChild(el)
    }
  }, [el])

  return ReactDOM.createPortal(props.children, el)
}

export default Modal
