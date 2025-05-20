import React, { memo, useState, ChangeEvent, useRef } from 'react';
// import { ReactComponent as PlusIcon } from '../../assets/icons/plus.svg';
// const PlusIcon = () => <span>+</span>;


const NewButton = memo(function NewButton() {
const [selectedFile, setSelectedFile] = useState<File | null>(null);
const fileInputRef = useRef<HTMLInputElement | null>(null);
const [statusMessage, setStatusMessage] = useState<string>(''); // 상태 메시지 포괄적 사용

const handleFileChange = (event: ChangeEvent<HTMLInputElement>) => {
if (event.target.files && event.target.files.length > 0) {
setSelectedFile(event.target.files[0]);
setStatusMessage(''); // 파일 변경 시 상태 메시지 초기화
} else {
setSelectedFile(null); // 파일 선택이 취소된 경우
setStatusMessage('');
}
};

// React <-> Spring Boot 연결 테스트 함수 (기존과 동일)
const handleReactSpringConnectionTest = async () => {
setStatusMessage('React-Spring 연결 테스트 요청 전송 중...');
try {
const payload = { message: "React에서 Spring Boot로 보내는 테스트 메시지입니다!" };
const response = await fetch("http://localhost:8080/api/react-test", {
method: "POST",
headers: {
"Content-Type": "application/json",
"Accept": "application/json", // 또는 text/plain 등 서버 응답에 맞게
},
body: JSON.stringify(payload),
});

const responseData = await response.text();

if (response.ok) {
console.log("React-Spring 연결 테스트 성공!");
setStatusMessage(`[연결 테스트] 서버 응답: ${response.status} - ${responseData}`);
} else {
console.error(`React-Spring 연결 테스트 실패: ${response.status} - ${responseData}`);
setStatusMessage(`[연결 테스트] 서버 오류: ${response.status} - ${responseData}`);
}
} catch (error) {
console.error("React-Spring 연결 테스트 중 네트워크 오류:", error);
setStatusMessage(`[연결 테스트] 네트워크 오류: ${error}`);
}
};

// 파일 업로드 함수 (기존과 동일)
const handleUpload = async () => {
if (selectedFile) {
setStatusMessage(`${selectedFile.name} 파일 업로드 중...`);
const formData = new FormData();
formData.append("file", selectedFile);

try {
const response = await fetch("http://localhost:8080/api/upload", {
method: "POST",
body: formData,
headers: {
"Accept": "application/json, text/plain, */*", // 서버 응답 타입 명시
},
// credentials: "include", // 필요시 주석 해제
});

const responseBodyText = await response.text();

if (response.ok) {
console.log("파일 업로드 성공!");
setStatusMessage(`[업로드] 성공: ${response.status} - ${responseBodyText}`);
} else {
console.error(`파일 업로드 실패: ${response.status} - ${responseBodyText}`);
setStatusMessage(`[업로드] 실패: ${response.status} - ${responseBodyText}`);
}
} catch (error) {
console.error("파일 업로드 중 네트워크 오류:", error);
setStatusMessage(`[업로드] 네트워크 오류: ${error}`);
}
} else {
console.error("선택된 파일이 없습니다!");
setStatusMessage("업로드할 파일이 선택되지 않았습니다!");
}
};

// --- 👇👇👇 새로운 파일 검증 함수 👇👇👇 ---
const handleVerify = async () => {
if (selectedFile) {
setStatusMessage(`${selectedFile.name} 파일 검증 요청 중...`);
const formData = new FormData();
// Spring Boot의 @RequestParam("file") 에 맞게 파일 추가
formData.append("file", selectedFile);

try {
// Spring Boot의 /api/verify-file 엔드포인트로 요청
const response = await fetch("http://localhost:8080/api/verify-file", {
method: "POST",
body: formData, // 파일만 보내면 Spring Boot 컨트롤러에서 @RequestParam("file")으로 받음
headers: {
// FormData 사용 시 Content-Type은 브라우저가 자동 설정
"Accept": "application/json, text/plain, */*", // 서버 응답 타입 명시
},
// credentials: "include", // 필요시 주석 해제
});

const responseBodyText = await response.text(); // 서버 응답은 텍스트로 가정 (JSON일 수도 있음)

if (response.ok) {
console.log("파일 검증 요청 성공!");
// 서버에서 JSON 형태의 상세 응답을 보냈다면 파싱해서 사용하는 것이 좋음
// 예: const responseData = JSON.parse(responseBodyText);
// setStatusMessage(`[검증] 결과: ${responseData.message}`);
setStatusMessage(`[검증] 결과: ${response.status} - ${responseBodyText}`);
} else {
console.error(`파일 검증 요청 실패: ${response.status} - ${responseBodyText}`);
setStatusMessage(`[검증] 실패: ${response.status} - ${responseBodyText}`);
}
} catch (error) {
console.error("파일 검증 요청 중 네트워크 오류:", error);
setStatusMessage(`[검증] 네트워크 오류: ${error}`);
}
} else {
console.error("선택된 파일이 없습니다!");
setStatusMessage("검증할 파일이 선택되지 않았습니다!");
}
};
// --- 👆👆👆 새로운 파일 검증 함수 끝 👆👆👆 ---

return (
<div>
{/* 연결 테스트 버튼 */}
<button type="button" onClick={handleReactSpringConnectionTest} style={{ marginRight: '10px', padding: '10px' }}>
React-Spring 연결 테스트
</button>

<hr style={{margin: '20px 0'}} />

{/* 파일 선택 버튼 */}
<input
type="file"
id="fileInput"
style={{ display: 'none' }}
onChange={handleFileChange}
ref={fileInputRef}
/>
<button type="button" onClick={() => fileInputRef.current?.click()} style={{ padding: '10px' }}>
{/* <PlusIcon /> */} + New (파일 선택)
</button>

{/* 파일 선택 시 나타나는 버튼들 */}
{selectedFile && (
<span style={{ marginLeft: '20px' }}>
<strong style={{ marginRight: '10px' }}>선택된 파일: {selectedFile.name}</strong>
<button type="button" onClick={handleUpload} style={{ marginRight: '10px', padding: '10px', backgroundColor: 'lightblue' }}>
이 파일 업로드 하기
</button>
{/* --- 👇👇👇 새로운 파일 검증 버튼 👇👇👇 --- */}
<button type="button" onClick={handleVerify} style={{ padding: '10px', backgroundColor: 'lightgreen' }}>
이 파일 검증하기
</button>
{/* --- 👆👆👆 새로운 파일 검증 버튼 끝 👆👆👆 --- */}
</span>
)}

{/* 상태 메시지 표시 */}
{statusMessage && (
<div style={{ marginTop: '20px', padding: '10px', border: '1px solid #ccc', whiteSpace: 'pre-wrap' }}>
<strong>상태 메시지:</strong><br />
{statusMessage}
</div>
)}
</div>
);
});

export default NewButton;