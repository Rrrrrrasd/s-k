package com.example.demo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping; // GetMapping 추가
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody; // RequestBody 추가
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
// React의 credentials: "include" 옵션에 대응하고, CORS 문제를 명확히 해결하기 위해 allowCredentials = "true" 추가
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class FileUploadController {

	private static final String UPLOAD_DIR = "uploads/";
	private static final String NODE_JS_GATEWAY_STORE_URL = "http://localhost:4000/store";
	private static final String NODE_JS_GATEWAY_TEST_URL = "http://localhost:4000/nodetest_from_spring";
	// private static final String WSL_NODE_IP = "172.17.226.23"; // 예:
	// "172.24.100.50"
	// private static final String NODE_JS_GATEWAY_STORE_URL = "http://" +
	// WSL_NODE_IP + ":4000/store";
	// private static final String NODE_JS_GATEWAY_TEST_URL = "http://" +
	// WSL_NODE_IP + ":4000/nodetest_from_spring";

// React 와의 기본 연결 테스트용 엔드포인트
	@PostMapping("/react-test")
	public ResponseEntity<String> handleReactConnectionTest(@RequestBody Map<String, String> payload) {
		System.out.println(">>> /api/react-test 엔드포인트 호출됨. React로부터 받은 메시지: " + payload.get("message"));
		return ResponseEntity.ok("Spring Boot: React로부터 메시지를 성공적으로 받았습니다! Timestamp: " + System.currentTimeMillis());
	}

// Spring Boot 에서 Node.js 게이트웨이로의 연결 테스트용 엔드포인트 (개발자용)
// 브라우저에서 GET http://localhost:8080/api/test-node-connection 으로 직접 호출하여 테스트
	@GetMapping("/test-node-connection")
	public ResponseEntity<String> testNodeConnection() {
		System.out.println(">>> /api/test-node-connection 호출됨. Node.js 게이트웨이 연결 테스트 시작...");
		RestTemplate restTemplate = new RestTemplate();
		try {
			System.out.println("Node.js 테스트 엔드포인트 (" + NODE_JS_GATEWAY_TEST_URL + ") 호출 시도...");
			ResponseEntity<String> nodeResponse = restTemplate.getForEntity(NODE_JS_GATEWAY_TEST_URL, String.class);
			String responseBody = nodeResponse.getBody();
			System.out.println("Node.js 게이트웨이로부터 응답 수신: " + responseBody);
			return ResponseEntity.ok("Node.js 연결 테스트 성공! 응답: " + responseBody);
		} catch (ResourceAccessException e) {
			System.err.println("!!!!!!!!!!!!!!!!!!!! Node.js 연결 테스트 실패 (ResourceAccessException): " + e.getMessage()
					+ " !!!!!!!!!!!!!!!!!!!!");
// e.printStackTrace(); // 필요시 전체 스택 트레이스 출력
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Node.js 연결 테스트 실패 (ResourceAccessException): 연결 거부 또는 I/O 오류 - " + e.getMessage());
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			System.err.println("!!!!!!!!!!!!!!!!!!!! Node.js 연결 테스트 실패 (HTTP 오류 " + e.getStatusCode() + "): "
					+ e.getResponseBodyAsString() + " !!!!!!!!!!!!!!!!!!!!");
// e.printStackTrace();
			return ResponseEntity.status(e.getStatusCode())
					.body("Node.js 연결 테스트 실패 (HTTP 오류 " + e.getStatusCode() + "): " + e.getResponseBodyAsString());
		} catch (Exception e) {
			System.err.println(
					"!!!!!!!!!!!!!!!!!!!! Node.js 연결 테스트 중 알 수 없는 오류 발생: " + e.getMessage() + " !!!!!!!!!!!!!!!!!!!!");
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Node.js 연결 테스트 중 알 수 없는 오류: " + e.getMessage());
		}
	}

	@PostMapping("/upload")
	public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
		System.out.println(">>> /api/upload 엔드포인트 호출됨. 파일명: " + (file != null ? file.getOriginalFilename() : "null"));
		if (file == null || file.isEmpty()) {
			System.out.println("파일이 비어있습니다.");
			return new ResponseEntity<>("파일을 선택하세요!", HttpStatus.BAD_REQUEST);
		}

		try {
// 1. 파일 저장
			File dir = new File(UPLOAD_DIR);
			if (!dir.exists()) {
				boolean created = dir.mkdirs();
				if (created) {
					System.out.println(UPLOAD_DIR + " 디렉토리가 생성되었습니다.");
				} else {
					System.err.println(UPLOAD_DIR + " 디렉토리 생성에 실패했습니다.");
// 실패 시에도 진행은 하되, 아래 Files.write에서 예외가 발생할 수 있음
				}
			}

			String filename = file.getOriginalFilename();
			Path path = Paths.get(UPLOAD_DIR + filename);
			Files.write(path, file.getBytes());
			System.out.println("파일이 성공적으로 저장되었습니다: " + path.toString());

// 2. SHA-256 해시 생성
			String fileHash = getSHA256Hash(file);
			System.out.println("파일 해시가 생성되었습니다: " + fileHash);

// 3. Node.js 게이트웨이로 페이로드 전송
			Map<String, String> payload = new HashMap<>();
			payload.put("fileId", filename);
			payload.put("fileHash", fileHash);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(payload, headers);
			RestTemplate restTemplate = new RestTemplate();

			System.out.println("Node.js 게이트웨이 (" + NODE_JS_GATEWAY_STORE_URL + ") 호출 시작. 페이로드: " + payload);
// Node.js 서버로 POST 요청
			ResponseEntity<String> nodeResponse = restTemplate.postForEntity(NODE_JS_GATEWAY_STORE_URL, requestEntity,
					String.class);
			System.out.println("Node.js 게이트웨이로부터 응답 수신. 상태 코드: " + nodeResponse.getStatusCode() + ", 본문: "
					+ nodeResponse.getBody());

			if (nodeResponse.getStatusCode() == HttpStatus.OK) {
				return new ResponseEntity<>("파일 업로드 및 블록체인 저장 요청 성공: " + nodeResponse.getBody(), HttpStatus.OK);
			} else {
// 이 경우는 Node.js 서버가 OK가 아닌 다른 상태 코드를 반환했을 때
				return new ResponseEntity<>("블록체인 저장 요청은 성공했으나, Node.js 서버로부터 오류 응답: " + nodeResponse.getBody(),
						nodeResponse.getStatusCode());
			}

		} catch (ResourceAccessException e) {
// Node.js 서버 연결 거부 또는 I/O 오류 시 발생
			System.err.println("!!!!!!!!!!!!!!!!!!!! 파일 업로드 중 Node.js 게이트웨이 호출 실패 (ResourceAccessException): "
					+ e.getMessage() + " !!!!!!!!!!!!!!!!!!!!");
// e.printStackTrace(); // 매우 상세한 로그가 필요할 때 주석 해제
			return new ResponseEntity<>(
					"서버 내부 오류: 블록체인 게이트웨이 통신 실패 (Connection refused 또는 I/O Error). 관리자에게 문의하세요. 상세: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (HttpClientErrorException | HttpServerErrorException e) {
// Node.js 서버가 4xx 또는 5xx 오류를 반환했을 때 발생
			System.err.println("!!!!!!!!!!!!!!!!!!!! 파일 업로드 중 Node.js 게이트웨이로부터 HTTP 오류 응답 (" + e.getStatusCode() + "): "
					+ e.getResponseBodyAsString() + " !!!!!!!!!!!!!!!!!!!!");
			return new ResponseEntity<>("서버 내부 오류: 블록체인 게이트웨이에서 오류 발생. 상세: " + e.getResponseBodyAsString(),
					e.getStatusCode());
		} catch (IOException e) {
			System.err.println(
					"!!!!!!!!!!!!!!!!!!!! 파일 저장/처리 중 IOException 발생: " + e.getMessage() + " !!!!!!!!!!!!!!!!!!!!");
			e.printStackTrace();
			return new ResponseEntity<>("서버 내부 오류: 파일 처리 중 오류가 발생했습니다. 상세: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
// 기타 모든 예외
			System.err.println("!!!!!!!!!!!!!!!!!!!! 파일 업로드 중 알 수 없는 심각한 오류 발생 !!!!!!!!!!!!!!!!!!!!");
			e.printStackTrace();
			return new ResponseEntity<>("서버 내부에서 알 수 없는 오류가 발생했습니다. 콘솔 로그를 확인해주세요. 상세: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/verify-file")
	public ResponseEntity<?> verifyFile(@RequestParam("file") MultipartFile fileToVerify) {
		if (fileToVerify.isEmpty()) {
			return new ResponseEntity<>("검증할 파일을 선택하세요!", HttpStatus.BAD_REQUEST);
		}

		try {
			String originalFilename = fileToVerify.getOriginalFilename();
			byte[] fileBytes = fileToVerify.getBytes();

			// 1. 검증할 파일의 해시 새로 계산
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] calculatedHashBytes = digest.digest(fileBytes);
			String calculatedHash = Base64.getEncoder().encodeToString(calculatedHashBytes);
			System.out.println("검증을 위해 새로 계산된 해시: " + calculatedHash + " (파일: " + originalFilename + ")");

			// 2. Node.js 게이트웨이의 /verify API 호출 준비
			RestTemplate restTemplate = new RestTemplate();
			String blockchainVerifyUrl = "http://localhost:4000/verify"; // Node.js Gateway 서버 주소 (실제 IP로 변경 필요시)

			Map<String, String> payload = new HashMap<>();
			payload.put("fileId", originalFilename);
			payload.put("hashToCheck", calculatedHash);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

			// 3. Node.js 게이트웨이에 검증 요청
			ResponseEntity<String> response = restTemplate.postForEntity(blockchainVerifyUrl, request, String.class);
			System.out.println("Node.js 로부터 받은 검증 결과: " + response.getBody());

			// Node.js 게이트웨이의 응답 (JSON)을 파싱하여 사용자에게 전달 (여기서는 간단히 문자열로 전달)
			// 실제로는 JSON 객체로 변환하여 verified 값에 따라 다른 메시지 처리 권장
			if (response.getStatusCode().is2xxSuccessful()) {
				// 예시: {"fileId":"슬픈 고냥쓰.jpg","verified":true,"message":"파일 무결성이 확인되었습니다."}
				// 이 문자열을 파싱해서 verified 값을 확인해야 함.
				return ResponseEntity.ok("블록체인 검증 결과: " + response.getBody());
			} else {
				return ResponseEntity.status(response.getStatusCode()).body("블록체인 검증 중 오류 발생: " + response.getBody());
			}

		} catch (NoSuchAlgorithmException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("해시 계산 알고리즘 오류: " + e.getMessage());
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 처리 오류: " + e.getMessage());
		} catch (RestClientException e) {
			System.err.println("Node.js 게이트웨이 호출 중 오류 발생: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("블록체인 게이트웨이 통신 오류: " + e.getMessage());
		}
	}

	private String getSHA256Hash(MultipartFile file) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] hashBytes = digest.digest(file.getBytes());
		return Base64.getEncoder().encodeToString(hashBytes);
	}
}