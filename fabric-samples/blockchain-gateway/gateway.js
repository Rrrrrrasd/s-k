'use strict'; // JavaScript의 엄격 모드를 사용합니다. 오류를 더 잘 잡아줍니다.

const express = require('express');
const { Gateway, Wallets } = require('fabric-network');
const path = require('path');
const fs = require('fs');
const bodyParser = require('body-parser');

const app = express();
app.use(bodyParser.json()); // JSON 요청 본문을 파싱하기 위한 미들웨어

console.log('Blockchain Gateway가 시작되었습니다. (gateway.js)');

// 모든 요청에 대해 상세 로그를 남기는 미들웨어 (기존 코드 유지)
app.use((req, res, next) => {
const timestamp = new Date().toISOString();
console.log(`\n[${timestamp}] --- 요청 수신 시작 ---`);
console.log(`메소드 & URL: ${req.method} ${req.originalUrl}`);
console.log('요청 헤더 전체:');
console.log(JSON.stringify(req.headers, null, 2));
console.log(`[${timestamp}] --- 요청 헤더 로깅 완료 ---`);
next();
});

// Spring Boot에서 Node.js 연결 테스트용 GET 엔드포인트 (기존 코드 유지)
app.get('/nodetest_from_spring', (req, res) => {
const timestamp = new Date().toISOString();
console.log(`\n[${timestamp}] >>> /nodetest_from_spring 엔드포인트 호출됨 (GET 요청)`);
const responseMessage = 'Node.js 게이트웨이로부터의 테스트 응답입니다! (Spring Boot 연결 테스트용)';
console.log(`응답 메시지: ${responseMessage}`);
res.status(200).send(responseMessage);
});

// 파일 ID와 해시를 받아 블록체인에 저장하는 API 엔드포인트 (기존 코드 유지, 약간의 오류 처리 개선)
app.post('/store', async (req, res) => {
const timestamp = new Date().toISOString();
console.log(`\n[${timestamp}] >>> /store API 엔드포인트 호출됨 (POST 요청)`);
console.log('요청 본문 (req.body):');
console.log(JSON.stringify(req.body, null, 2));

const { fileId, fileHash } = req.body;

if (!fileId || !fileHash) {
console.error('[POST /store] 오류: fileId 또는 fileHash가 요청 본문에 없습니다.');
return res.status(400).send('요청 오류: fileId와 fileHash는 필수입니다.');
}

let gateway; // gateway 객체를 try 블록 외부에서도 접근 가능하도록 선언
try {
const ccpPath = path.resolve(__dirname, 'connection.json');
if (!fs.existsSync(ccpPath)) {
console.error(`[POST /store] Fabric 네트워크 연결 설정 파일(connection.json)을 찾을 수 없습니다: ${ccpPath}`);
return res.status(500).send('서버 설정 오류: Fabric 네트워크 연결 파일을 찾을 수 없습니다.');
}
const ccp = JSON.parse(fs.readFileSync(ccpPath, 'utf8'));

const walletPath = path.join(__dirname, 'wallet');
const wallet = await Wallets.newFileSystemWallet(walletPath);

const identity = await wallet.get('appUser');
if (!identity) {
console.error('[POST /store] appUser 인증서가 wallet에 존재하지 않습니다. registerUser.js를 먼저 실행하세요.');
return res.status(401).send('appUser 인증서가 없어 Fabric 네트워크에 접근할 수 없습니다.');
}

gateway = new Gateway(); // gateway 객체 생성
console.log('[POST /store] Fabric 게이트웨이에 연결 시도 중...');
await gateway.connect(ccp, {
wallet,
identity: 'appUser',
discovery: { enabled: true, asLocalhost: true },
});
console.log('[POST /store] Fabric 게이트웨이에 성공적으로 연결되었습니다.');

const network = await gateway.getNetwork('mychannel');
console.log('[POST /store] 채널 "mychannel" 에 연결되었습니다.');
const contract = network.getContract('filecontract');
console.log('[POST /store] 체인코드 "filecontract" 를 가져왔습니다.');

console.log(`[POST /store] 체인코드 함수 'storeFileHash' 호출 시도: fileId=${fileId}, fileHash=${fileHash}`);
await contract.submitTransaction('storeFileHash', fileId, fileHash);
console.log('[POST /store] 트랜잭션이 성공적으로 제출되었습니다.');

res.status(200).send(`파일 정보 [${fileId}]가 블록체인에 성공적으로 저장되었습니다.`);

} catch (error) {
console.error(`[POST /store] !!!!!!!!!!!!!!!!!!!! Fabric 네트워크/체인코드 호출 중 심각한 오류 발생 !!!!!!!!!!!!!!!!!!!!`);
console.error('[POST /store] 오류 객체 전체:', error);
console.error('[POST /store] 오류 스택 트레이스: ', error.stack || ' 스택 트레이스 없음');

let clientErrorMessage = '블록체인 처리 중 내부 서버 오류가 발생했습니다.';
if (error.message) {
clientErrorMessage += ` 상세: ${error.message}`;
}
res.status(500).send(clientErrorMessage);
} finally {
// try 블록이 성공하든 실패하든 항상 게이트웨이 연결 해제 시도
if (gateway && gateway.getIdentity()) { // gateway 객체가 생성되었고, 연결된 상태라면
try {
await gateway.disconnect();
console.log('[POST /store] Fabric 게이트웨이 연결이 해제되었습니다.');
} catch (disconnectError) {
console.error('[POST /store] 게이트웨이 연결 해제 중 추가 오류:', disconnectError);
}
}
}
});


// --- 👇👇👇 새로운 조회 API 엔드포인트 추가 부분 👇👇👇 ---
app.get('/query/:fileId', async (req, res) => {
const timestamp = new Date().toISOString();
const { fileId } = req.params; // URL 경로에서 fileId 파라미터 추출 (예: /query/내파일.txt)

console.log(`\n[${timestamp}] >>> /query/:fileId API 엔드포인트 호출됨 (GET 요청)`);
console.log(`[GET /query] 조회 요청된 fileId: ${fileId}`);

if (!fileId) {
console.error('[GET /query] 오류: fileId가 URL 경로에 없습니다.');
return res.status(400).send('요청 오류: fileId는 URL 경로에 필수로 포함되어야 합니다. (예: /query/filename.txt)');
}

let gateway; // gateway 객체를 try 블록 외부에서도 접근 가능하도록 선언
try {
const ccpPath = path.resolve(__dirname, 'connection.json');
if (!fs.existsSync(ccpPath)) {
console.error(`[GET /query] Fabric 네트워크 연결 설정 파일(connection.json)을 찾을 수 없습니다: ${ccpPath}`);
return res.status(500).send('서버 설정 오류: Fabric 네트워크 연결 파일을 찾을 수 없습니다.');
}
const ccp = JSON.parse(fs.readFileSync(ccpPath, 'utf8'));

const walletPath = path.join(__dirname, 'wallet');
const wallet = await Wallets.newFileSystemWallet(walletPath);

const identity = await wallet.get('appUser');
if (!identity) {
console.error('[GET /query] appUser 인증서가 wallet에 존재하지 않습니다. registerUser.js를 먼저 실행하세요.');
return res.status(401).send('appUser 인증서가 없어 Fabric 네트워크에 접근할 수 없습니다.');
}

gateway = new Gateway(); // gateway 객체 생성
console.log('[GET /query] Fabric 게이트웨이에 연결 시도 중...');
await gateway.connect(ccp, {
wallet,
identity: 'appUser',
discovery: { enabled: true, asLocalhost: true },
});
console.log('[GET /query] Fabric 게이트웨이에 성공적으로 연결되었습니다.');

const network = await gateway.getNetwork('mychannel');
console.log('[GET /query] 채널 "mychannel" 에 연결되었습니다.');
const contract = network.getContract('filecontract');
console.log('[GET /query] 체인코드 "filecontract" 를 가져왔습니다.');

console.log(`[GET /query] 체인코드 함수 'queryFileHash' 호출 시도: fileId=${fileId}`);
// 원장 상태를 변경하지 않는 읽기 전용이므로 evaluateTransaction 사용
const resultBytes = await contract.evaluateTransaction('queryFileHash', fileId);
const resultString = resultBytes.toString(); // 체인코드에서 문자열로 반환한다고 가정
console.log(`[GET /query] 체인코드로부터 받은 조회 결과: ${resultString}`);

// 체인코드에서 "정보 없음" 등의 메시지를 반환한 경우, 이를 클라이언트에 맞게 가공
if (resultString.includes("정보가 원장에 없습니다") || resultString.includes("does not exist")) {
console.log(`[GET /query] fileId '${fileId}' 에 대한 정보가 블록체인에 없습니다.`);
res.status(404).send({ fileId: fileId, message: `'${fileId}'에 대한 정보를 찾을 수 없습니다.` });
} else {
// 성공적으로 해시값을 조회한 경우
res.status(200).send({ fileId: fileId, storedHash: resultString });
}

} catch (error) {
console.error(`[GET /query] !!!!!!!!!!!!!!!!!!!! Fabric 네트워크/체인코드 호출 중 심각한 오류 발생 !!!!!!!!!!!!!!!!!!!!`);
console.error('[GET /query] 오류 객체 전체:', error);
console.error('[GET /query] 오류 스택 트레이스: ', error.stack || ' 스택 트레이스 없음');

// 체인코드에서 `throw new Error`로 발생시킨 오류는 error.message 또는 error.responses에서 확인 가능
let clientErrorMessage = '블록체인 조회 중 내부 서버 오류가 발생했습니다.';
if (error.message) {
// message에 'does not exist' 또는 '정보가 원장에 없습니다' 와 유사한 패턴이 있으면 404로 응답
if (error.message.toLowerCase().includes('does not exist') || error.message.includes('정보가 원장에 없습니다')) {
console.log(`[GET /query] fileId '${fileId}' 에 대한 정보 조회 실패 (체인코드 오류 메시지): ${error.message}`);
return res.status(404).send({ fileId: fileId, message: `'${fileId}'에 대한 정보를 찾을 수 없습니다 (상세: ${error.message})` });
}
clientErrorMessage += ` 상세: ${error.message}`;
}
res.status(500).send(clientErrorMessage);
} finally {
// try 블록이 성공하든 실패하든 항상 게이트웨이 연결 해제 시도
if (gateway && gateway.getIdentity()) { // gateway 객체가 생성되었고, 연결된 상태라면
try {
await gateway.disconnect();
console.log('[GET /query] Fabric 게이트웨이 연결이 해제되었습니다.');
} catch (disconnectError) {
console.error('[GET /query] 게이트웨이 연결 해제 중 추가 오류:', disconnectError);
}
}
}
});
// --- 👆👆👆 새로운 조회 API 엔드포인트 추가 부분 끝 👆👆👆 ---

// --- 👇👇👇 새로운 파일 검증 API 엔드포인트 (POST 요청) 👇👇👇 ---
app.post('/verify', async (req, res) => {
    const timestamp = new Date().toISOString();
    console.log(`\n[${timestamp}] >>> /verify API 엔드포인트 호출됨 (POST 요청)`);
    console.log('[POST /verify] 요청 본문 (req.body):');
    console.log(JSON.stringify(req.body, null, 2));
    
    const { fileId, hashToCheck } = req.body; // 클라이언트가 보낸 파일 ID와 새로 계산한 해시
    
    if (!fileId || !hashToCheck) {
    console.error('[POST /verify] 오류: fileId 또는 hashToCheck가 요청 본문에 없습니다.');
    return res.status(400).send('요청 오류: fileId와 hashToCheck는 필수입니다.');
    }
    
    let gateway;
    try {
    const ccpPath = path.resolve(__dirname, 'connection.json');
    // ... (ccp, wallet, identity 로드 로직은 /store, /query 와 유사하게 작성) ...
    if (!fs.existsSync(ccpPath)) {
    console.error(`[POST /verify] Fabric 네트워크 연결 설정 파일(connection.json)을 찾을 수 없습니다: ${ccpPath}`);
    return res.status(500).send('서버 설정 오류: Fabric 네트워크 연결 파일을 찾을 수 없습니다.');
    }
    const ccp = JSON.parse(fs.readFileSync(ccpPath, 'utf8'));
    
    const walletPath = path.join(__dirname, 'wallet');
    const wallet = await Wallets.newFileSystemWallet(walletPath);
    
    const identity = await wallet.get('appUser');
    if (!identity) {
    console.error('[POST /verify] appUser 인증서가 wallet에 존재하지 않습니다.');
    return res.status(401).send('appUser 인증서가 없어 Fabric 네트워크에 접근할 수 없습니다.');
    }
    
    gateway = new Gateway();
    console.log('[POST /verify] Fabric 게이트웨이에 연결 시도 중...');
    await gateway.connect(ccp, {
    wallet,
    identity: 'appUser',
    discovery: { enabled: true, asLocalhost: true },
    });
    console.log('[POST /verify] Fabric 게이트웨이에 성공적으로 연결되었습니다.');
    
    const network = await gateway.getNetwork('mychannel');
    console.log('[POST /verify] 채널 "mychannel" 에 연결되었습니다.');
    const contract = network.getContract('filecontract');
    console.log('[POST /verify] 체인코드 "filecontract" 를 가져왔습니다.');
    
    console.log(`[POST /verify] 체인코드 함수 'verifyFile' 호출 시도: fileId=${fileId}, hashToCheck=${hashToCheck}`);
    // verifyFile 함수는 true/false를 반환하므로 evaluateTransaction 사용 가능
    const resultBytes = await contract.evaluateTransaction('verifyFile', fileId, hashToCheck);
    const isVerified = resultBytes.toString() === 'true'; // 체인코드에서 boolean을 반환하면 "true" 또는 "false" 문자열로 옴
    
    console.log(`[POST /verify] 체인코드로부터 받은 검증 결과: ${isVerified}`);
    
    res.status(200).send({ fileId: fileId, verified: isVerified, message: isVerified ? "파일 무결성이 확인되었습니다." : "파일이 변경되었거나 원본이 아닙니다." });
    
    } catch (error) {
    console.error(`[POST /verify] !!!!!!!!!!!!!!!!!!!! Fabric 네트워크/체인코드 호출 중 심각한 오류 발생 !!!!!!!!!!!!!!!!!!!!`);
    console.error('[POST /verify] 오류 객체 전체:', error);
    console.error('[POST /verify] 오류 스택 트레이스: ', error.stack || ' 스택 트레이스 없음');
    
    let clientErrorMessage = '블록체인 검증 중 내부 서버 오류가 발생했습니다.';
    // 체인코드에서 throw new Error 한 경우 (예: 파일 없음)
    if (error.message && error.message.includes('존재하지 않음')) {
    console.log(`[POST /verify] 파일 '${fileId}' 가 블록체인에 존재하지 않아 검증 불가: ${error.message}`);
    return res.status(404).send({ fileId: fileId, verified: false, message: `파일 '${fileId}' 가 블록체인에 존재하지 않아 검증할 수 없습니다.` });
    }
    if (error.message) {
    clientErrorMessage += ` 상세: ${error.message}`;
    }
    res.status(500).send(clientErrorMessage);
    } finally {
    if (gateway && gateway.getIdentity()) {
    try {
    await gateway.disconnect();
    console.log('[POST /verify] Fabric 게이트웨이 연결이 해제되었습니다.');
    } catch (disconnectError) {
    console.error('[POST /verify] 게이트웨이 연결 해제 중 추가 오류:', disconnectError);
    }
    }
    }
    });
    // --- 👆👆👆 새로운 파일 검증 API 엔드포인트 끝 👆👆👆 ---


const PORT = process.env.PORT || 4000; // 환경변수 PORT가 있으면 사용, 없으면 4000 사용
app.listen(PORT, () => {
console.log(`\nBlockchain Gateway (gateway.js) 서버가 ${PORT}번 포트에서 실행 중입니다.`);
console.log(`POST /store : 파일 ID와 해시를 받아 블록체인에 저장`);
console.log(`GET /query/:fileId : 파일 ID로 블록체인에서 저장된 해시 조회 (신규 추가됨!)`);
console.log(`GET /nodetest_from_spring : Spring Boot 연결 테스트`);
console.log('--- 서버 준비 완료 ---');
});