import moment from 'moment'
import { ReactComponent as FolderIcon } from './assets/icons/folder.svg'
import Header from './components/Header'
import LeftAsideColumn from './components/LeftAsideColumn'
import PathBar from './components/PathBar'
import Tooltip from './components/Tooltip'
import files from './data/files.json'
import folders from './data/folders.json'
import { AContainer, AContent, AContentTable, AMain } from './styles'
import getIcon from './utils/getIcon'

function App() {
  // useEffect(() => {
  //   function onClick(event: MouseEvent) {
  //     console.clear()
  //     console.log(event.composedPath())
  //     console.log(event.target)
  //   }

  //   document.addEventListener('click', onClick)

  //   return () => {
  //     document.removeEventListener('click', onClick)
  //   }
  // }, [])

  return (
    <AContainer>
      <Header />
      <LeftAsideColumn />

      <AMain>
        <PathBar />

        <AContent>
          <AContentTable>
            <thead>
              <tr>
                <th>Name</th>
                <th>Owner</th>
                <th>Last modified</th>
                <th>File size</th>
              </tr>
            </thead>

            <tbody>
              {folders.map(folder => (
                <tr key={folder.id}>
                  <td>
                    <div>
                      <FolderIcon />
                    </div>

                    <span>{folder.name}</span>
                  </td>
                  <td>me</td>
                  <td>
                    {moment(folder.updatedAt).format('MMM DD, yyyy')}
                    <span>me</span>
                  </td>
                  <td>-</td>
                </tr>
              ))}

              {files.map(file => (
                <tr key={file.id}>
                  <td>
                    <div>
                      <img src={getIcon(file.type)} alt="" />
                    </div>

                    <span>{file.name}</span>
                  </td>
                  <td>me</td>
                  <td>
                    {moment(file.updatedAt).format('MMM DD, yyyy')}
                    <span>me</span>
                  </td>
                  <td>
                    {file.size >= 1024
                      ? `${file.size / 1024} MB`
                      : `${file.size} KB`}
                  </td>
                </tr>
              ))}
            </tbody>
          </AContentTable>
        </AContent>
      </AMain>

      <Tooltip />
    </AContainer>
  )
}

export default App
