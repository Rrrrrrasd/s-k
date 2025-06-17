package com.contract.backend.controller;

import com.contract.backend.common.Entity.ContractEntity;
import com.contract.backend.common.Entity.ContractVersionEntity;
import com.contract.backend.common.Entity.UserEntity;
import com.contract.backend.common.exception.CustomException;
import com.contract.backend.common.exception.CustomExceptionEnum;
import com.contract.backend.common.repository.ContractPartyRepository;
import com.contract.backend.common.repository.ContractVersionRepository;
import com.contract.backend.service.AuthService;
import com.contract.backend.service.S3StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import jakarta.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/contracts/files")
public class ContractFileController {

    private static final Logger logger = LoggerFactory.getLogger(ContractFileController.class);
    private static final Pattern RANGE_PATTERN = Pattern.compile("bytes=(\\d+)-(\\d*)");

    private final S3StorageService s3StorageService;
    private final AuthService authService;
    private final ContractVersionRepository contractVersionRepository;
    private final ContractPartyRepository contractPartyRepository;
    private final S3Client s3Client;

    public ContractFileController(
            S3StorageService s3StorageService,
            AuthService authService,
            ContractVersionRepository contractVersionRepository,
            ContractPartyRepository contractPartyRepository,
            S3Client s3Client
    ) {
        this.s3StorageService = s3StorageService;
        this.authService = authService;
        this.contractVersionRepository = contractVersionRepository;
        this.contractPartyRepository = contractPartyRepository;
        this.s3Client = s3Client;
    }

    /**
     * 계약서 파일 미리보기 (스트리밍 지원)
     * 경로 변수 대신 쿼리 파라미터 사용
     */
    @GetMapping("/preview")
    public ResponseEntity<Resource> previewContractFile(
            @RequestParam("path") String filePath,  // 쿼리 파라미터로 변경
            @AuthenticationPrincipal String userUuid,
            @RequestHeader(value = "Range", required = false) String rangeHeader,
            HttpServletRequest request
    ) {
        try {
            logger.info("파일 미리보기 요청 - filePath: {}, userUuid: {}, range: {}", filePath, userUuid, rangeHeader);

            // 1. 사용자 인증
            UserEntity user = authService.findByUuid(userUuid);

            // 2. 파일 경로로 계약서 버전 찾기 및 권한 검증
            ContractVersionEntity contractVersion = findContractVersionByFilePath(filePath);
            validateUserAccess(user, contractVersion.getContract());

            // 3. S3에서 파일 정보 조회
            String bucketName = s3StorageService.getBucketName();
            HeadObjectResponse headResponse = getFileMetadata(bucketName, filePath);
            long fileSize = headResponse.contentLength();
            String contentType = determineContentType(headResponse.contentType());

            // 4. Range 헤더 처리
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                return handleRangeRequest(bucketName, filePath, rangeHeader, fileSize, contentType);
            } else {
                return handleFullRequest(bucketName, filePath, fileSize, contentType);
            }

        } catch (CustomException e) {
            logger.error("권한 오류 - filePath: {}, userUuid: {}, error: {}", filePath, userUuid, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("파일 미리보기 중 오류 발생 - filePath: {}, error: {}", filePath, e.getMessage(), e);
            throw new RuntimeException("파일을 불러올 수 없습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 계약서 파일 다운로드
     * 쿼리 파라미터 방식으로 변경
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadContractFile(
            @RequestParam("path") String filePath,  // 쿼리 파라미터로 변경
            @AuthenticationPrincipal String userUuid
    ) {
        try {
            logger.info("파일 다운로드 요청 - filePath: {}, userUuid: {}", filePath, userUuid);

            // 1. 사용자 인증 및 권한 검증
            UserEntity user = authService.findByUuid(userUuid);
            ContractVersionEntity contractVersion = findContractVersionByFilePath(filePath);
            validateUserAccess(user, contractVersion.getContract());

            // 2. S3에서 파일 다운로드
            String bucketName = s3StorageService.getBucketName();
            ResponseInputStream<GetObjectResponse> s3Object = downloadFromS3(bucketName, filePath);
            
            // 3. 파일명 생성
            String fileName = generateFileName(contractVersion);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .body(new InputStreamResource(s3Object));

        } catch (CustomException e) {
            logger.error("다운로드 권한 오류 - filePath: {}, userUuid: {}, error: {}", filePath, userUuid, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("파일 다운로드 중 오류 발생 - filePath: {}, error: {}", filePath, e.getMessage(), e);
            throw new RuntimeException("파일 다운로드에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * S3에서 파일 메타데이터 조회
     */
    private HeadObjectResponse getFileMetadata(String bucketName, String filePath) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .build();
            return s3Client.headObject(headRequest);
        } catch (Exception e) {
            logger.error("S3 파일 메타데이터 조회 실패 - bucket: {}, key: {}", bucketName, filePath, e);
            throw new RuntimeException("파일 정보를 가져올 수 없습니다", e);
        }
    }

    /**
     * S3에서 파일 다운로드
     */
    private ResponseInputStream<GetObjectResponse> downloadFromS3(String bucketName, String filePath) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .build();
            return s3Client.getObject(getRequest);
        } catch (Exception e) {
            logger.error("S3 파일 다운로드 실패 - bucket: {}, key: {}", bucketName, filePath, e);
            throw new RuntimeException("파일을 다운로드할 수 없습니다", e);
        }
    }

    /**
     * Content-Type 결정
     */
    private String determineContentType(String s3ContentType) {
        if (s3ContentType == null || !s3ContentType.equals("application/pdf")) {
            return "application/pdf"; // 기본값으로 PDF 설정
        }
        return s3ContentType;
    }

    /**
     * Range 요청 처리 (부분 콘텐츠 스트리밍)
     */
    private ResponseEntity<Resource> handleRangeRequest(
            String bucketName, 
            String filePath, 
            String rangeHeader, 
            long fileSize, 
            String contentType
    ) {
        try {
            // Range 헤더 파싱
            RangeInfo rangeInfo = parseRangeHeader(rangeHeader, fileSize);
            if (rangeInfo == null) {
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .header("Content-Range", "bytes */" + fileSize)
                        .build();
            }

            long contentLength = rangeInfo.end - rangeInfo.start + 1;

            // S3에서 Range 요청
            ResponseInputStream<GetObjectResponse> s3Object = downloadRangeFromS3(
                    bucketName, filePath, rangeInfo.start, rangeInfo.end);

            logger.debug("Range 요청 처리 - start: {}, end: {}, contentLength: {}", 
                    rangeInfo.start, rangeInfo.end, contentLength);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength))
                    .header(HttpHeaders.CONTENT_RANGE, 
                            "bytes " + rangeInfo.start + "-" + rangeInfo.end + "/" + fileSize)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600") // 1시간 캐시
                    .body(new InputStreamResource(s3Object));

        } catch (Exception e) {
            logger.error("Range 요청 처리 중 오류: {}", e.getMessage(), e);
            throw new RuntimeException("부분 콘텐츠 요청 처리 실패", e);
        }
    }

    /**
     * 전체 파일 요청 처리
     */
    private ResponseEntity<Resource> handleFullRequest(
            String bucketName, 
            String filePath, 
            long fileSize, 
            String contentType
    ) {
        try {
            ResponseInputStream<GetObjectResponse> s3Object = downloadFromS3(bucketName, filePath);

            logger.debug("전체 파일 요청 처리 - fileSize: {}", fileSize);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize))
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600") // 1시간 캐시
                    .body(new InputStreamResource(s3Object));

        } catch (Exception e) {
            logger.error("전체 파일 요청 처리 중 오류: {}", e.getMessage(), e);
            throw new RuntimeException("파일 요청 처리 실패", e);
        }
    }

    /**
     * Range 헤더 파싱
     */
    private RangeInfo parseRangeHeader(String rangeHeader, long fileSize) {
        Matcher matcher = RANGE_PATTERN.matcher(rangeHeader);
        if (!matcher.matches()) {
            return null;
        }

        long start = Long.parseLong(matcher.group(1));
        long end = matcher.group(2).isEmpty() ? fileSize - 1 : Long.parseLong(matcher.group(2));

        // 범위 검증
        if (start >= fileSize || end >= fileSize || start > end) {
            return null;
        }

        return new RangeInfo(start, end);
    }

    /**
     * S3에서 Range 요청으로 파일 다운로드
     */
    private ResponseInputStream<GetObjectResponse> downloadRangeFromS3(
            String bucketName, String filePath, long start, long end) {
        try {
            GetObjectRequest rangeRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .range("bytes=" + start + "-" + end)
                    .build();
            return s3Client.getObject(rangeRequest);
        } catch (Exception e) {
            logger.error("S3 Range 요청 실패 - bucket: {}, key: {}, range: {}-{}", 
                    bucketName, filePath, start, end, e);
            throw new RuntimeException("부분 파일을 다운로드할 수 없습니다", e);
        }
    }

    /**
     * 파일 경로로 계약서 버전 찾기
     */
    private ContractVersionEntity findContractVersionByFilePath(String filePath) {
        return contractVersionRepository.findByFilePathAndContractNotDeleted(filePath)
                .orElseThrow(() -> {
                    logger.error("파일 경로에 해당하는 계약서 버전을 찾을 수 없음: {}", filePath);
                    return new CustomException(CustomExceptionEnum.CONTRACT_NOT_FOUND);
                });
    }

    /**
     * 사용자 접근 권한 검증
     */
    private void validateUserAccess(UserEntity user, ContractEntity contract) {
        boolean isCreator = contract.getCreatedBy().getId().equals(user.getId());
        boolean isParticipant = contractPartyRepository.findByContractAndParty(contract, user).isPresent();

        if (!isCreator && !isParticipant) {
            logger.warn("파일 접근 권한 없음 - contractId: {}, userUuid: {}", contract.getId(), user.getUuid());
            throw new CustomException(CustomExceptionEnum.UNAUTHORIZED);
        }
    }

    /**
     * 다운로드용 파일명 생성
     */
    private String generateFileName(ContractVersionEntity contractVersion) {
        String contractTitle = contractVersion.getContract().getTitle();
        int versionNumber = contractVersion.getVersionNumber();
        
        // 파일명에서 특수문자 제거
        String sanitizedTitle = contractTitle.replaceAll("[^a-zA-Z0-9가-힣\\s]", "");
        
        return String.format("%s_v%d.pdf", sanitizedTitle, versionNumber);
    }

    /**
     * Range 정보를 담는 내부 클래스
     */
    private static class RangeInfo {
        final long start;
        final long end;

        RangeInfo(long start, long end) {
            this.start = start;
            this.end = end;
        }
    }
}