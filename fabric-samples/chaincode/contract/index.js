'use strict';

const { Contract } = require('fabric-contract-api');

class FileContract extends Contract {
async initLedger(ctx) {
console.info('Chaincode 초기화 완료');
}

async storeFileHash(ctx, fileId, fileHash) {
console.info(`원장에 저장 시도: fileId='${fileId}', fileHash='${fileHash}'`);
await ctx.stub.putState(fileId, Buffer.from(fileHash)); // fileHash를 Buffer로 변환하여 저장
console.info(`저장 완료: ${fileId}`);
return `파일 해시 저장 완료: ${fileId}`;
}

async verifyFile(ctx, fileId, hashToCheck) {
const storedHashBytes = await ctx.stub.getState(fileId);
if (!storedHashBytes || storedHashBytes.length === 0) {
throw new Error(`파일 ${fileId} 가 원장에 존재하지 않습니다.`);
}
// 원장에서 가져온 해시 (Buffer)를 문자열로 변환하여 비교
const storedHashString = storedHashBytes.toString();
console.info(`검증 요청: fileId='${fileId}', 저장된 해시='${storedHashString}', 비교할 해시='${hashToCheck}'`);
return storedHashString === hashToCheck;
}

// --- 여기 새로운 조회 함수 추가 ---
async queryFileHash(ctx, fileId) {
console.info(`원장에서 해시 조회 시도: fileId='${fileId}'`);
const storedHashBytes = await ctx.stub.getState(fileId); // fileId를 키로 사용하여 상태 조회

if (!storedHashBytes || storedHashBytes.length === 0) {
console.info(`조회 실패: fileId='${fileId}' 에 해당하는 정보 없음`);
// throw new Error(`${fileId} does not exist`); // 에러를 던지거나,
return `${fileId}에 해당하는 해시 정보가 원장에 없습니다.`; // 혹은 특정 메시지를 반환
}

// getState는 byte 배열(Buffer)을 반환. storeFileHash에서 Buffer.from(fileHash)로 저장했으므로,
// 다시 문자열로 변환하여 반환합니다.
const storedHashString = storedHashBytes.toString();
console.info(`조회 성공: fileId='${fileId}', 저장된 해시='${storedHashString}'`);
return storedHashString; // 저장된 해시 문자열 자체를 반환
}
}

module.exports = FileContract;
