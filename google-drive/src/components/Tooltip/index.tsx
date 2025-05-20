import { useEffect, useState } from 'react'
import Modal from '../Modal'
import { TContainer } from './styles'

function Tooltip() {
  const [value, setValue] = useState('')
  const [position, setPosition] = useState({ x: 0, y: 0 })
  const [direction, setDirection] = useState<'end' | 'start'>()

  useEffect(() => {
    function enterEvent(event: Event) {
      const element = event.target as HTMLElement
      const label = element.getAttribute('aria-label')
      const direction = element.getAttribute('data-tooltip-align')
      const noTooltip = element.getAttribute('data-no-tooltip')

      if (!label || noTooltip) return

      const { height, width, x, y } = element.getBoundingClientRect()
      let pX = x + width / 2

      if (direction === 'end') pX = x + width
      else if (direction === 'start') pX = x

      setValue(label)
      setPosition({ x: pX, y: y + height + 2.5 })
      setDirection(direction as 'end' | 'start' | undefined)
    }

    function leaveEvent() {
      setValue('')
    }

    const elements = document.querySelectorAll('[aria-label]')
    elements.forEach(element => {
      element.addEventListener('mouseenter', enterEvent)
      element.addEventListener('mouseleave', leaveEvent)
    })

    return () => {
      elements.forEach(element => {
        element.removeEventListener('mouseenter', enterEvent)
        element.removeEventListener('mouseleave', leaveEvent)
      })
    }
  }, [])

  if (!value) return null

  return (
    <Modal>
      <TContainer position={position} direction={direction}>
        <div>
          {value.includes('Google Account:') ? (
            <>
              <p>Google Account:</p>
              <p>
                {value
                  .substring(value.indexOf(':') + 1, value.indexOf('('))
                  .trim()}
              </p>
              <p>
                {value
                  .substring(value.indexOf('(') + 1, value.indexOf(')'))
                  .trim()}
              </p>
            </>
          ) : (
            value
          )}
        </div>
      </TContainer>
    </Modal>
  )
}

export default Tooltip
