import documentsImg from '../assets/icons/documents.png'
import imageImg from '../assets/icons/image.png'
import pdfImg from '../assets/icons/pdf.png'
import presentationImg from '../assets/icons/presentation.png'
import spreadsheetImg from '../assets/icons/spreadsheet.png'
import videoImg from '../assets/icons/video.png'

function getIcon(type: string) {
  if (type.includes('pdf')) return pdfImg
  else if (type.includes('image')) return imageImg
  else if (type.includes('video')) return videoImg
  else if (type.includes('msword')) return documentsImg
  else if (type.includes('vnd.ms-excel')) return spreadsheetImg
  else if (type.includes('vnd.ms-powerpoint')) return presentationImg
  else return undefined
}

export default getIcon
