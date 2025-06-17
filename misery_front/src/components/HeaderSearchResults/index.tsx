// src/components/HeaderSearchResults/index.tsx
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
import { getMyContracts } from '../../utils/api'

interface IProps {
  value: string
  onContractClick?: (contractId: number) => void // 계약서 클릭 핸들러 추가
}

interface IResults {
  id: string
  name: string
  type: string
  createdBy: string
  updatedAt: number
}

interface ContractListItem {
  id: number;
  title: string;
  status: 'OPEN' | 'CLOSED' | 'CANCELLED';
  createdAt: string;
  currentVersionNumber?: number;
}

function HeaderSearchResults({ value, onContractClick }: IProps) {
  const [results, setResults] = useState<IResults[] | null>(null)
  const [contracts, setContracts] = useState<ContractListItem[]>([])
  const [contractsLoading, setContractsLoading] = useState(false)

  // 계약서 목록 로드
  useEffect(() => {
    const loadContracts = async () => {
      try {
        setContractsLoading(true)
        const response = await getMyContracts(0, 20) // 최대 20개까지
        if (response.success) {
          setContracts(response.data.content || [])
        }
      } catch (error) {
        console.error('계약서 목록 로드 실패:', error)
        setContracts([])
      } finally {
        setContractsLoading(false)
      }
    }

    loadContracts()
  }, [])

  // 검색 로직
  useEffect(() => {
    if (!value) {
      setResults(null)
    } else {
      // 기존 파일 검색
      const fileResults = files.filter(file => {
        return file.name.toLowerCase().includes(value.toLowerCase())
      })

      setResults(fileResults)
    }
  }, [value])

  // 계약서 검색 결과 필터링
  const filteredContracts = value 
    ? contracts.filter(contract => 
        contract.title.toLowerCase().includes(value.toLowerCase())
      )
    : contracts.slice(0, 5) // 검색어가 없으면 최근 5개만 표시

  const getStatusText = (status: string) => {
    switch (status) {
      case 'OPEN': return '진행중'
      case 'CLOSED': return '완료'
      case 'CANCELLED': return '취소됨'
      default: return status
    }
  }

  const getStatusStyle = (status: string) => {
    switch (status) {
      case 'OPEN': return { color: '#1976d2' }
      case 'CLOSED': return { color: '#388e3c' }
      case 'CANCELLED': return { color: '#d32f2f' }
      default: return {}
    }
  }

  // 계약서 클릭 핸들러
  const handleContractClick = (contractId: number) => {
    if (onContractClick) {
      onContractClick(contractId)
    }
  }

  if (results === null) {
    return (
      <HSRContainer>
        <HSRContent>
          {/* 계약서 섹션 - 기본으로 표시 */}
          {contractsLoading ? (
            <div style={{ 
              padding: '16px', 
              textAlign: 'center', 
              color: '#5f6368' 
            }}>
              계약서 로딩 중...
            </div>
          ) : (
            <>
              <div style={{ 
                padding: '8px 16px', 
                fontSize: '0.875rem', 
                fontWeight: 500, 
                color: '#5f6368',
                borderBottom: '1px solid #dadce0'
              }}>
                내 계약서 {contracts.length > 0 && `(${Math.min(contracts.length, 5)}개)`}
              </div>
              
              {contracts.length > 0 ? (
                filteredContracts.map(contract => (
                  <ListTile
                    key={contract.id}
                    icon={<img src={pdfImg} alt="" />}
                    title={contract.title}
                    subtitle={`${getStatusText(contract.status)} • v${contract.currentVersionNumber || 1}`}
                    subtitleStyle={getStatusStyle(contract.status)}
                    trailing={moment(contract.createdAt).format('DD/MM/YY')}
                    onClick={() => handleContractClick(contract.id)}
                  />
                ))
              ) : (
                <div style={{ 
                  padding: '16px', 
                  textAlign: 'center', 
                  color: '#5f6368',
                  fontSize: '0.875rem'
                }}>
                  아직 계약서가 없습니다.
                </div>
              )}
            </>
          )}

          {/* 구분선 */}
          {!contractsLoading && contracts.length > 0 && (
            <div style={{ 
              height: '1px', 
              backgroundColor: '#dadce0', 
              margin: '8px 0' 
            }} />
          )}

          {/* 기존 파일 타입 섹션 */}
          <div style={{ 
            padding: '8px 16px', 
            fontSize: '0.875rem', 
            fontWeight: 500, 
            color: '#5f6368',
            borderBottom: '1px solid #dadce0'
          }}>
            파일 타입별 검색
          </div>
          
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
        {/* 계약서 검색 결과 */}
        {filteredContracts.length > 0 && (
          <>
            <div style={{ 
              padding: '8px 16px', 
              fontSize: '0.875rem', 
              fontWeight: 500, 
              color: '#5f6368',
              borderBottom: '1px solid #dadce0'
            }}>
              계약서 ({filteredContracts.length}개)
            </div>
            {filteredContracts.map(contract => (
              <ListTile
                key={contract.id}
                icon={<img src={pdfImg} alt="" />}
                title={contract.title}
                subtitle={`${getStatusText(contract.status)} • v${contract.currentVersionNumber || 1}`}
                subtitleStyle={getStatusStyle(contract.status)}
                trailing={moment(contract.createdAt).format('DD/MM/YY')}
                onClick={() => handleContractClick(contract.id)}
              />
            ))}
          </>
        )}

        {/* 파일 검색 결과 */}
        {results.length === 0 && filteredContracts.length === 0 ? (
          <p>
            검색 결과가 없습니다. <span>다른 검색어를 시도해보세요.</span>
          </p>
        ) : results.length > 0 ? (
          <>
            <div style={{ 
              padding: '8px 16px', 
              fontSize: '0.875rem', 
              fontWeight: 500, 
              color: '#5f6368',
              borderBottom: '1px solid #dadce0'
            }}>
              파일 ({results.length}개)
            </div>
            {results.map(file => (
              <ListTile
                key={file.id}
                icon={<img src={getIcon(file.type)} alt="" />}
                title={file.name}
                subtitle={file.createdBy}
                trailing={moment(file.updatedAt).format('DD/MM/YY')}
              />
            ))}
          </>
        ) : null}
      </HSRContent>
    </HSRContainer>
  )
}

export default HeaderSearchResults