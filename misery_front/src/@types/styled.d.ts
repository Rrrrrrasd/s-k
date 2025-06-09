import 'styled-components'
import colors from '../theme/colors'
import shadows from '../theme/shadows'

declare module 'styled-components' {
  export interface DefaultTheme {
    colors: typeof colors
    shadows: typeof shadows
  }
}
