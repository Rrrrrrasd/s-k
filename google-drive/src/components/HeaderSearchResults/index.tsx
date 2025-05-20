import moment from 'moment'
import { useEffect, useState } from 'react'
import documentsImg from '../../assets/icons/documents.png'
import imageImg from '../../assets/icons/image.png'
import pdfImg from '../../assets/icons/pdf.png'
import presentationImg from '../../assets/icons/presentation.png'
import spreadsheetImg from '../../assets/icons/spreadsheet.png'
import videoImg from '../../assets/icons/video.png'
import files from '../../data/files.json'
import getIcon from '../../utils/getIcon'
import ListTile from '../ListTile'
import { HSRContainer, HSRContent, HSRMoreResults } from './styles'

interface IProps {
  value: string
}

interface IResults {
  id: string
  name: string
  type: string
  createdBy: string
  updatedAt: number
}

function HeaderSearchResults({ value }: IProps) {
  const [results, setResults] = useState<IResults[] | null>(null)

  useEffect(() => {
    if (!value) setResults(null)
    else {
      const newResults = files.filter(file => {
        return file.name.toLowerCase().includes(value.toLowerCase())
      })

      setResults(newResults)
    }
  }, [value])

  if (results === null) {
    return (
      <HSRContainer>
        <HSRContent>
          <ListTile title="PDFs" icon={<img src={pdfImg} alt="" />} />

          <ListTile
            title="Documents"
            icon={<img src={documentsImg} alt="" />}
          />

          <ListTile
            title="Spreadsheets"
            icon={<img src={spreadsheetImg} alt="" />}
          />
          <ListTile
            title="Presentations"
            icon={<img src={presentationImg} />}
          />

          <ListTile
            title="Photos & images"
            icon={<img src={imageImg} alt="" />}
          />

          <ListTile title="Videos" icon={<img src={videoImg} alt="" />} />

          <HSRMoreResults title="More search tools" />
        </HSRContent>
      </HSRContainer>
    )
  }

  return (
    <HSRContainer>
      <HSRContent>
        {results.length === 0 ? (
          <p>
            No recent item match your search. <span>Search older items.</span>
          </p>
        ) : (
          results.map(file => (
            <ListTile
              key={file.id}
              icon={<img src={getIcon(file.type)} alt="" />}
              title={file.name}
              subtitle={file.createdBy}
              trailing={moment(file.updatedAt).format('DD/MM/YY')}
            />
          ))
        )}
      </HSRContent>
    </HSRContainer>
  )
}

export default HeaderSearchResults
