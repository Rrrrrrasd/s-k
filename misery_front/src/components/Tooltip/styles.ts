import styled from 'styled-components'

interface ITContainer {
  position: { x: number; y: number }
  direction?: 'end' | 'start'
}

export const TContainer = styled.div<ITContainer>`
  top: ${({ position }) => position.y}px;
  left: ${({ position }) => position.x}px;
  position: absolute;

  display: flex;
  align-items: flex-start;
  justify-content: ${({ direction }) =>
    !direction
      ? 'center'
      : direction === 'end'
      ? 'flex-end'
      : direction === 'start'
      ? 'flex-start'
      : 'center'};

  & > div {
    padding: 4px 8px;

    bottom: auto;
    position: absolute;

    font: 600 0.75rem/1.5 'Roboto', sans-serif;
    color: #fff;
    letter-spacing: 0.05rem;

    white-space: nowrap;

    border-radius: 0.25rem;
    background-color: rgba(79, 83, 85, 255);

    & > p + p {
      color: #babec3;
      font: inherit;
      letter-spacing: inherit;
    }
  }
`
