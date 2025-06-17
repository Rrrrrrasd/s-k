# Misery - 블록체인 기반 전자계약 관리 플랫폼

Misery는 Hyperledger Fabric 블록체인 기술과 생체 인증(WebAuthn/Passkey)을 활용하여 계약서의 무결성과 보안을 강화한 전자계약 관리 플랫폼입니다. 사용자는 안전하게 계약서를 업로드하고, 버전 관리를 하며, 참여자들과 함께 서명 프로세스를 진행할 수 있습니다. 모든 계약의 중요 기록은 블록체인에 저장되어 위변조를 방지합니다.

##  주요 기능

### 백엔드 (Misery-Back)
- **사용자 인증**: JWT 토큰 기반의 기본 로그인 및 WebAuthn(Passkey)을 이용한 강력한 2단계 인증을 지원합니다.
- **계약서 관리**: 계약서의 생성, 조회, 수정, 삭제(논리적 삭제) 기능을 제공합니다.
- **버전 관리**: 계약서 수정 시 새로운 버전을 생성하고 이전 버전을 보관하여 변경 이력을 추적합니다.
- **폴더 관리**: 사용자는 폴더를 생성하여 계약서를 체계적으로 관리할 수 있습니다.
- **전자 서명**: 계약 참여자들은 업로드된 계약서에 전자 서명을 할 수 있습니다.
- **블록체인 연동**:
    - 계약서의 메타데이터(파일 해시, 참여자, 서명 정보 등)를 Hyperledger Fabric 블록체인에 기록하여 데이터의 무결성을 보장합니다.
    - 블록체인에 기록된 데이터와 현재 DB 데이터를 비교하여 무결성을 검증하는 기능을 제공합니다.
- **파일 저장**: 계약서 파일은 B2 Cloud Storage와 같은 S3 호환 스토리지에 안전하게 저장됩니다.

### 프론트엔드 (Misery-Front)
- **사용자 인터페이스**: React와 Styled-components를 사용한 직관적이고 반응형 웹 UI를 제공합니다.
- **인증**: 이메일/비밀번호 로그인, 회원가입 및 Passkey 등록/로그인 기능을 제공합니다.
- **대시보드**: 사용자는 자신의 계약서와 폴더를 한눈에 보고 관리할 수 있습니다.
- **계약서 관리**:
    - 새로운 계약서를 업로드하고 참여자를 지정할 수 있습니다.
    - 기존 계약서의 내용을 수정하고 새 버전을 업로드할 수 있습니다.
    - PDF 뷰어를 통해 계약서 내용을 모달 창에서 바로 확인할 수 있습니다.
- **폴더 기능**: 폴더 생성, 이름 변경, 계약서 이동 등 파일 탐색기와 유사한 기능을 제공합니다.
- **통합 검색**: 계약서 제목 및 파일 이름으로 원하는 문서를 빠르게 찾을 수 있습니다.

## 기술 스택

| 구분 | 기술 | 설명 |
|---|---|---|
| **백엔드** | Java 17, Spring Boot | 안정적이고 생산성 높은 백엔드 개발 환경 |
| | Spring Security, JWT | 토큰 기반의 안전한 인증 및 인가 처리 |
| | JPA (Hibernate), MySQL | 데이터 영속성 관리 및 관계형 데이터베이스 사용 |
| | Hyperledger Fabric | 계약 데이터의 무결성 보장을 위한 프라이빗 블록체인 |
| | WebAuthn (Yubico) | Passkey를 이용한 암호 없는 강력한 인증 |
| | Gradle | 의존성 관리 및 빌드 자동화 |
| **프론트엔드** | React, TypeScript | 타입 안정성을 갖춘 컴포넌트 기반 UI 개발 |
| | Vite | 빠르고 효율적인 프론트엔드 빌드 및 개발 환경 |
| | Styled-components | 동적 스타일링을 위한 CSS-in-JS 라이브러리 |
| | React Router | 클라이언트 사이드 라우팅 관리 |
| | React-PDF | 웹 페이지 내 PDF 문서 렌더링 |
| **공통/인프라**| B2 Cloud Storage (S3 호환) | 계약서 파일의 안전한 객체 스토리지 |
| | WSL2, Docker | 개발 환경 격리 및 블록체인 네트워크 구동 |

## 프로젝트 구조
├── misery_back/     # Spring Boot 백엔드 서버
│   ├── src/
│   └── build.gradle
└── misery_front/    # React 프론트엔드
├── src/
└── package.json


## 설치 및 실행 방법

### 1. 사전 준비
- **WSL2**: Windows 사용자의 경우, Linux 환경을 위해 WSL2를 설치합니다.
- **Docker & Docker Compose**: 블록체인 네트워크와 데이터베이스 구동을 위해 필요합니다.
- **Go (1.20.x 권장)**: Hyperledger Fabric 체인코드 컴파일을 위해 필요합니다.
- **JDK (17 이상)**: 백엔드 서버 구동을 위해 필요합니다.
- **Node.js (18.x 권장)**: 프론트엔드 개발 환경 구성을 위해 필요합니다.

### 2. Hyperledger Fabric 네트워크 설정
백엔드 프로젝트의 `misery_back/README.md` 파일에 상세한 네트워크 설정 가이드가 포함되어 있습니다. 아래는 주요 단계 요약입니다.

> **참고**: 모든 명령어는 WSL2 터미널에서 실행해야 합니다.

1.  **Fabric Samples 클론**:
    ```bash
    git clone --branch v2.4.9 [https://github.com/hyperledger/fabric-samples.git](https://github.com/hyperledger/fabric-samples.git)
    cd fabric-samples
    ```

2.  **바이너리 파일 다운로드**:
    ```bash
    curl -sSLO [https://raw.githubusercontent.com/hyperledger/fabric/main/scripts/install-fabric.sh](https://raw.githubusercontent.com/hyperledger/fabric/main/scripts/install-fabric.sh) && chmod +x install-fabric.sh
    ./install-fabric.sh docker samples binary
    ```

3.  **네트워크 실행 및 체인코드 배포**:
    ```bash
    cd test-network
    ./network.sh down # 기존 네트워크 정리
    ./network.sh up
    ./network.sh createChannel
    ./network.sh deployCC -ccn basic -ccp ../asset-transfer-basic/chaincode-go -ccl go
    ```
    - **체인코드 수정**: `fabric-samples/asset-transfer-basic/chaincode-go/chaincode/smartcontract.go` 파일에 `misery_back/README.md`에 명시된 `CreateContractMetadataRecord`와 `ReadContractMetadataRecord` 함수를 추가해야 합니다.

4.  **인증서 및 설정 파일 복사**:
    - 백엔드 애플리케이션이 Fabric 네트워크와 통신하기 위해 필요한 인증서와 연결 프로필 파일을 로컬 PC의 특정 경로(예: `C:/fabric-config`)로 복사해야 합니다. 자세한 경로는 `misery_back/README.md`를 참고하세요.

### 3. 백엔드 실행

1.  **`application-secret.yml` 설정**:
    `misery_back/src/main/resources/` 경로에 `application-secret.yml` 파일을 생성하고, 데이터베이스, JWT, Fabric 네트워크, S3 스토리지 접속 정보를 입력합니다.
    ```yaml
    spring:
      datasource:
        password: YOUR_DB_PASSWORD

    jwt:
      secret: YOUR_JWT_SECRET_KEY
      expiration: 86400000 # 24시간
      refreshExpiration: 604800000 # 7일

    b2:
      endpoint: YOUR_B2_ENDPOINT # 예: s3.us-west-004.backblazeb2.com
      access-key: YOUR_B2_ACCESS_KEY
      secret-key: YOUR_B2_SECRET_KEY
      bucket-name: YOUR_B2_BUCKET_NAME

    fabric:
      channelName: "mychannel"
      chaincodeName: "basic"
      mspId: "Org1MSP"
      userIdentity: "Admin@org1.example.com"
      credentials:
        certificatePath: "C:/fabric-config/crypto/org1/users/Admin@org1.example.com/msp/signcerts/Admin@org1.example.com-cert.pem"
        privateKeyPath: "C:/fabric-config/crypto/org1/users/Admin@org1.example.com/msp/keystore/priv_sk"
      gateway:
        peerEndpoint: "localhost:7051"
        tlsCaCertPath: "C:/fabric-config/crypto/org1/tlsca/tlsca.org1.example.com-cert.pem"
        overrideAuth: "peer0.org1.example.com"
    ```

2.  **애플리케이션 실행**:
    `misery_back` 디렉토리에서 아래 명령어를 실행하여 Spring Boot 애플리케이션을 시작합니다.
    ```bash
    ./gradlew bootRun
    ```

### 4. 프론트엔드 실행

1.  **의존성 설치**:
    `misery_front` 디렉토리로 이동하여 필요한 라이브러리를 설치합니다.
    ```bash
    cd misery_front
    npm install
    ```

2.  **개발 서버 실행**:
    아래 명령어로 Vite 개발 서버를 시작합니다.
    ```bash
    npm run dev
    ```
    - 실행 후 `https://localhost:5173`으로 접속할 수 있습니다.
