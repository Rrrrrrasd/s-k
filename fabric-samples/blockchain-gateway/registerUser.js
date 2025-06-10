const { Wallets, Gateway } = require('fabric-network');
const FabricCAServices = require('fabric-ca-client');
const path = require('path');
const fs = require('fs');

async function main() {
try {
// 1. Fabric 네트워크 연결 설정 파일(connection-org1.json) 로드
// 이 경로는 registerUser.js 파일의 위치를 기준으로 test-network 내의 connection-org1.json을 가리켜야 합니다.
// 현재 코드는 registerUser.js가 application-gateway 폴더에 있고,
// application-gateway 폴더와 test-network 폴더가 fabric-samples 바로 아래에 같은 레벨로 있다고 가정합니다.
// 예: fabric-samples/application-gateway/registerUser.js
//     fabric-samples/test-network/organizations/peerOrganizations/org1.example.com/connection-org1.json
const ccpPath = path.resolve(__dirname, '..', 'test-network', 'organizations', 'peerOrganizations', 'org1.example.com', 'connection-org1.json');
const ccp = JSON.parse(fs.readFileSync(ccpPath, 'utf8'));

// 2. CA(Certificate Authority) 클라이언트 생성
// connection-org1.json 파일에서 CA 서버 URL을 가져옵니다.
const caURL = ccp.certificateAuthorities['ca.org1.example.com'].url;
const ca = new FabricCAServices(caURL);

// 3. Wallet 생성 (또는 기존 Wallet 사용)
// 이 wallet은 registerUser.js 스크립트가 실행되는 위치에 생성됩니다.
// 생성된 후에는 blockchain-gateway/wallet/ 디렉토리로 옮겨야 합니다.
const walletPath = path.join(__dirname, 'wallet'); // 현재 디렉토리 밑에 wallet 폴더 생성
const wallet = await Wallets.newFileSystemWallet(walletPath);
console.log(`Wallet 경로: ${walletPath}`);

// 4. 이미 appUser가 존재하는지 확인
const identity = await wallet.get('appUser');
if (identity) {
console.log('appUser 사용자는 이미 wallet에 존재합니다.');
return;
}

// 5. Admin 사용자 인증서 확인 (appUser를 등록할 권한이 있는 사용자)
const adminIdentity = await wallet.get('admin');
if (!adminIdentity) {
// Admin 인증서가 없으면 오류 메시지 출력 후 종료.
// Admin을 먼저 등록하는 enrollAdmin.js (또는 유사 스크립트)가 선행되어야 합니다.
// fabric-samples의 application-javascript 예제에는 보통 enrollAdmin.js가 포함되어 있습니다.
// 이 enrollAdmin.js를 먼저 실행해서 admin 사용자를 wallet에 등록해야 합니다.
console.log('Admin 사용자가 wallet에 존재하지 않습니다. enrollAdmin.js를 먼저 실행하여 Admin을 등록하세요.');
console.log('Hint: fabric-samples/application-javascript/enrollAdmin.js 를 참고하거나 실행하세요.');
return;
}

// 6. Admin 사용자 컨텍스트 가져오기
const provider = wallet.getProviderRegistry().getProvider(adminIdentity.type);
const adminUser = await provider.getUserContext(adminIdentity, 'admin');

// 7. CA에 appUser 등록 요청 및 secret(등록 비밀번호) 받기
const secret = await ca.register({
affiliation: 'org1.department1', // 조직 내 소속 (org1.example.com의 경우)
enrollmentID: 'appUser',         // 등록할 사용자 ID
role: 'client'                   // 역할 (일반 클라이언트)
}, adminUser);                     // Admin 권한으로 요청

// 8. 받은 secret으로 appUser 등록(enroll) 및 인증서/개인키 받기
const enrollment = await ca.enroll({
enrollmentID: 'appUser',
enrollmentSecret: secret
});

// 9. X.509 형식의 신원 정보 생성
const x509Identity = {
credentials: {
certificate: enrollment.certificate,      // 발급받은 인증서
privateKey: enrollment.key.toBytes(),   // 발급받은 개인키
},
mspId: 'Org1MSP',                         // 조직의 MSP ID (Org1의 경우)
type: 'X.509',
};

// 10. 생성된 appUser의 신원 정보를 Wallet에 저장
await wallet.put('appUser', x509Identity);
console.log('appUser 사용자가 성공적으로 등록되었고 wallet에 저장되었습니다.');
console.log('이제 "fabric-samples/application-gateway/wallet/" 폴더(또는 registerUser.js가 생성한 wallet 폴더)의 내용을 "blockchain-gateway/wallet/" 폴더로 복사해주세요.');

} catch (error) {
console.error(`appUser 등록 실패: ${error.stack || error}`);
process.exit(1); // 오류 발생 시 스크립트 종료
}
}

main();