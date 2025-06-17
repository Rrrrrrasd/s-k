# 계약 목록 조회, 계약 내용 상세 조회 API 추가, 계약서 다운 API는 추가안함 필요하면 말해주셈

# WSL2 기준 네트워크 설정 - 별도로 말안했으면 전부다 wsl 터미널에서 실시하면됨(wsl 쓰기 귀찮아용)
## 여기까지의 내용은 아무 기능없이 단순한 블록체인 네트워크를 만들어서 배포하는 과정 -> 아직 블록체인 네트워크에는 아무런 기능이 존재하지 않음

## 1. wsl 설치
>```wsl --intsall```
## 2. wsl 터미널에서 (update 및 git 설치)
>```sudo apt update```
>
>```sudo apt install git -y```
>git이 정상적으로 설치되었는지 확인
>```git --version```

## 3. wsl 터미널에서 (cURL 설치 + jq설치)
>```
>sudo apt install curl -y
>sudo apt install jq -y
>```
>버전이 정상적으로 나오면 설치완료
>```
>curl --version
>jq --version
>```

## 4. Docker 설치- 이후 확인
>wsl과 Docker가 통합되었는지 확인
>``` docker --version ```
>``` docker compose version```
>``` docker ps```
> * 안된다면 해당 명령어 실행후 해볼것
    > ```sudo usermod -aG docker ${USER} ```
> * WSL 세션 완전히 종료 후 재시작 (Windows PowerShell/CMD에서 wsl --shutdown 후 WSL 터미널 다시 실행)
> * 참고 docker ps의 결과는 이런식으로 나와야함
> ```
> CONTAINER ID   IMAGE     COMMAND   CREATED   STATUS    PORTS     NAMES
> ```

## 5. wsl 터미널에서 (Go 언어 설치) - Java, JavaScript로 배포도 가능한것같지만 Go로 설명
> 환경설정까지 완료우 정상적으로 Go가 설치되었는지 확인하기 위해 version 확인
```
wget https://go.dev/dl/go1.20.13.linux-amd64.tar.gz
sudo rm -rf /usr/local/go && sudo tar -C /usr/local -xzf go1.20.13.linux-amd64.tar.gz
echo 'export PATH=$PATH:/usr/local/go/bin' >> ~/.bashrc
echo 'export GOPATH=$HOME/go' >> ~/.bashrc
echo 'export PATH=$PATH:$GOPATH/bin' >> ~/.bashrc
source ~/.bashrc
go version
```

## 6. wsl 터미널에서 (Node.js 및 npm 설치) - 만약 javascript로 배포하겠다 그러면 설치(안해도 상관없지만 권장: 내가 함)

```
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh | bash
source ~/.bashrc # 또는 source ~/.nvm/nvm.sh
nvm install 18 # Node.js LTS 버전 (예: 18.x) 설치
nvm use 18
node -v
npm -v
```
## 7. wsl 터미널에서 (JDK 설치) - 만약 JAVA로 배포하겠다 그러면 설치(안해도 상관없지만 권장: 내가 함)

```
sudo apt install openjdk-11-jdk -y # 또는 openjdk-17-jdk 등
java --version
```

## 8. wsl 터미널에서 (JDK 설치) - 만약 Python으로 배포하겠다 그러면 설치(안해도 상관없지만 권장: 내가 함)
>
```
sudo apt install python3 python3-pip -y
python3 --version
```

## 9. 윗단계로 wsl 환경설정 완료 -> 본격적으로 test-network 설정

## 10. 루트로 가서 설치할 폴더 생성

```
mkdir ~/hyperledger-fabric-setup
cd ~/hyperledger-fabric-setup
```

## 11. 생성한 폴더에 git clone 떄리기

``` 
git clone --branch v2.4.9 https://github.com/hyperledger/fabric-samples.git
```

## 12. 그럼 fabric-samples 폴더가 생성됨 거기로 이동

```
cd fabric-samples
```

## 13. 혹시 모르니 install-fabric.sh 스크립트 다운로드 및 실행 권한 부여를 주세용

```
curl -sSLO https://raw.githubusercontent.com/hyperledger/fabric/main/scripts/install-fabric.sh && chmod +x install-fabric.sh
```

## 14. Docker 이미지 및 바이너리 다운로드 스크립트 실행

```
./install-fabric.sh docker samples binary
```

## 15. 고럼 PATH 환경변수 설정해주기 - 전 아직도 이걸 왜하는지 모르겠어용

```
echo "export PATH=${HOME}/fabric-dev/fabric-samples/bin:\$PATH" >> ~/.bashrc
```

## 16. 변경사항 적용시키기

```
source ~/.bashrc
```

## 17. 이제 네트워크 구성하러 가기 (이대로 따라왔으면 ~/hyperledger-fabric-setup/fabric-samples/test-network )

```
cd test-network
```

## 18. 찐빠 방지 기존 네트워크 정리

```
./network.sh down
```

## 19. 네트워크 올리기

```
./network.sh up
```

## 20. 채널 생성 (기본 채널명: mychannel )

```
./network.sh createChannel
```

## 20-1. 여기서 에러가 많이 발생할 수 있음
> #### a. jq가 정상적으로 설치되었는지 확인
> #### b. 기존의 mychannel이 존재 할 수 도 있음 -> 지워줘야함
>
```
./network.sh down
# WSL 터미널에서 실행
docker stop $(docker ps -a -q)
docker rm $(docker ps -a -q)
docker volume prune # (y 입력) (필요시, 하지만 주의)
docker network prune # (y 입력) (필요시, 하지만 주의)
# docker image prune # (필요시, 하지만 주의)

#네트워크 재시작 및 채널 재생성
# 현재 위치: ~/hyperledger-fabric-setup/fabric-samples/test-network
./network.sh up
./network.sh createChannel
```
> #### c. go버전이 안맞을 수 도 있음
```
 cd ~/hyperledger-fabric-setup/fabric-samples/asset-transfer-basic/chaincode-go
``` 
```
 nano go.mod
```
```
 # go 1.23.0 #이런식으로 버전이 맞춰져 있는 구문이 있다면 해당 버전을 go.1.20으로 수정해줌
 # go 1.20 
 #이후 저장하고
 # go mod tidy로 go.mod 업데이트 해주기
> cd ~/hyperledger-fabric-setup/fabric-samples/test-network #돌아가기
```


## 21. 체인코드 배포

```
./network.sh deployCC -ccn basic -ccp ../asset-transfer-basic/chaincode-go -ccl go
```




# ==============================================================================

# 요거슨 하다가 먼가 잘못됐다 느끼면 따라가볼만한 가이드 라인
## 1. 만들어놨던 디렉토리 삭제

```
rm -rf ~/fabric-dev
```

## 2. Docker 정리

```
docker stop $(docker ps -aq)
docker rm $(docker ps -aq)
```

## 3. 볼륨 삭제(이 프로젝트와 관련없는 다른 프로젝트의 볼륨이 있다면 그것도 삭제되니 이명령으로 하지말고 따른 명령어 사용하세용)

```
docker volume prune #주의
```

## 4. Docker 네트워크 삭제 ( 3번과 같은 이유)

```
docker network prune #주의
```

## 5. Docker 이미지 삭제 (3번과 같은 이유)

```
docker image prune -a
```

## 6. 환경변수 설정한거 초기화
>
```
nano ~/.bashrc
```
이거 실행 후 ctrl + O , ctrl + X 로 종료
이후 변경사항 적용
```
source ~/.bashrc
```

## 7. Docker 재시작
>
- Docker Desktop 설정 > Resources > WSL Integration 에서 사용하려는 Linux 배포판(예: Ubuntu)과의 통합이 켜져 있는지 확인

## 8. 다시 맨 위의 1번부터 재시도



# ===============================
# 블록체인 체인코드 추가(해당 가이드 라인을 따라왔을 경우)

```
cd ~/hyperledger-fabric-setup/fabric-samples/asset-transfer-basic/chaincode-go/chaincode
nano smartcontract.go
```
### smartcontract.go 에서 기존 코드 아래에 해당 함수들을 추가
- CreateContractMetadataRecord 함수
```GO
// CreateContractMetadataRecord stores a new contract metadata record in the world state.
// id: a unique identifier for the metadata record (e.g., "CONTRACT_VERSION_" + contractVersion.getId())
// metadataJson: a JSON string containing the contract metadata
func (s *SmartContract) CreateContractMetadataRecord(ctx contractapi.TransactionContextInterface, id string, metadataJson string) error {
	// Check if the asset already exists
	exists, err := s.AssetExists(ctx, id) // AssetExists는 asset-transfer-basic의 기존 함수 활용
	if err != nil {
		return fmt.Errorf("failed to read from world state: %v", err)
	}
	if exists {
		// If we don't want to allow updates via this function, return an error.
		// Or, if updates are allowed, this could be the logic to update an existing record.
		// For now, let's assume new records should not overwrite existing ones with the same ID via this function.
		return fmt.Errorf("the metadata record %s already exists", id)
	}

	// Put the metadata JSON string into the world state
	err = ctx.GetStub().PutState(id, []byte(metadataJson))
	if err != nil {
		return fmt.Errorf("failed to put metadata record in world state: %v", err)
	}
	// Optionally, you can return a success message or the ID itself if needed,
	// but usually, returning nil on success is standard for create/update operations.
	// The transaction ID will be available to the client application from the SDK response.
	return nil
}
```
- ReadContractMetadataRecord 함수
```go
// ReadContractMetadataRecord retrieves a contract metadata record from the world state.
// id: the unique identifier for the metadata record
func (s *SmartContract) ReadContractMetadataRecord(ctx contractapi.TransactionContextInterface, id string) (string, error) {
	metadataBytes, err := ctx.GetStub().GetState(id)
	if err != nil {
		return "", fmt.Errorf("failed to read metadata record %s from world state: %v", id, err)
	}
	if metadataBytes == nil {
		// It's important to decide how to handle "not found".
		// Returning an error is one way. Returning an empty string or a specific DTO might be another.
		// The Java service (HyperledgerFabricService) should be prepared to handle this.
		// For example, it might throw a specific exception if the record is not found.
		return "", fmt.Errorf("metadata record %s does not exist", id)
	}

	// Return the metadata as a JSON string
	return string(metadataBytes), nil
}
```

- if AssetExists 함수가 안보인다면 추가해주기 (보통 있음)
```angular2html
// AssetExists returns true when asset with given ID exists in world state
func (s *SmartContract) AssetExists(ctx contractapi.TransactionContextInterface, id string) (bool, error) {
	assetJSON, err := ctx.GetStub().GetState(id)
	if err != nil {
		return false, fmt.Errorf("failed to read from world state: %v", err)
	}

	return assetJSON != nil, nil
}
```

### 이러면 블록체인은 구현 끝
# ===============================

# 구현 다해놨고 이재 껏다가 켜고싶을때
## 1. 끄기
```
cd ~/hyperledger-fabric-setup/fabric-samples/test-network
./network.sh down
```

## 1-1. 재시작하려는데 먼가 문제가 발생한다?
```angular2html
cd ~/hyperledger-fabric-setup/fabric-samples/test-network
./network.sh down
docker stop $(docker ps -aq)
docker rm $(docker ps -aq)
docker volume prune #사용시 주의
docker network prune #사용시 주의
```

## 2. 재시작 -> 채널 생성, 체인코드 배포도 다시해줘야함
```
cd ~/hyperledger-fabric-setup/fabric-samples/test-network
./network.sh up
./network.sh createChannel
./network.sh deployCC -ccn basic -ccp ../asset-transfer-basic/chaincode-go -ccl go
```

## 3. 블록체인 경로 설정
우리는 로컬에 애플리케이션이 있고 wsl 환경에 네트워크가 존재 경로를 설정하는데 wsl 환경 경로를 잘 인식을 못함
그래서 wsl환경의 경로를 로컬로 옮겨서 강제로 인식하게 할것 
- 이과정은 네트워크를 껏다가 켤때마다 해줘야함(즉 배포를 다시 할때마다 새로 해줘야함)

1. 카톡으로 보낸 application-secret.yml의 fabric부분의 특정 경로를 맞춰주면됨
2. 내가 설정한 대로 폴더를 만들어도되고 마음대로 커스텀해도 상관없음
3. 수정을 해야할 부분은 이것들
```angular2html
walletPath: "C:/fabric-config/wallet"
connectionProfilePath: "C:/fabric-config/profiles/connection-org1.json"
certificatePath: "C:/fabric-config/crypto/org1/users/Admin@org1.example.com/msp/signcerts/Admin@org1.example.com-cert.pem"
privateKeyPath: "C:/fabric-config/crypto/org1/users/Admin@org1.example.com/msp/keystore/priv_sk"
tlsCaCertPath: "C:/fabric-config/crypto/org1/tlsca/tlsca.org1.example.com-cert.pem"
```
4. 위의 경로에 맞게 각각의 파일들을 가상환경에서 때와야함
5. wallet은 그냥 빈 폴더로 만들기
6. .../test-network 에서 아래의 명령 실행(경로 나옴)
```angular2html
explorer.exe .
```
7. 각 경로 정리
```angular2html
...\test-network\organizations\peerOrganizations\org1.example.com
#이 경로에 connection-org1.json과 connection-org1.yaml이 존재 둘다 C:/.../profiles 안에 복사

#같은 경로에 tlsca 폴더와 users 폴더가 존재 각각 3번의 폴더에 맞게끔해서 복사해 주면됨
```

8. 이걸 네트워크 껏다킬때마다 해줘야함.... 안하는방법이 있기는한데 너무 귀찮아요






