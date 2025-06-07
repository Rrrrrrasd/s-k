package com.contract.backend.controller;

import com.contract.backend.common.Entity.ContractEntity;
import com.contract.backend.common.Entity.ContractPartyEntity;
import com.contract.backend.common.Entity.UserEntity;
import com.contract.backend.common.dto.*;
import com.contract.backend.common.response.ApiResponse; // 추가 (ApiResponse 사용을 위해)
import com.contract.backend.service.AuthService;
import com.contract.backend.service.ContractService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    private final ContractService contractService;
    private final AuthService authService;

    public ContractController(
            ContractService contractService,
            AuthService authService
    ) {
        this.contractService = contractService;
        this.authService = authService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<ContractEntity>> uploadContract( // 반환 타입을 ApiResponse<ContractEntity>로 변경
                                                                       @RequestPart("data") ContractUploadRequestDTO request,
                                                                       @RequestPart("file") MultipartFile file,
                                                                       @AuthenticationPrincipal String uuid
    ) {
        try {
            UserEntity user = authService.findByUuid(uuid);
            ContractEntity contract = contractService.uploadContract(request, user, file);
            return ResponseEntity.ok(ApiResponse.success(contract)); // 성공 시 ApiResponse 반환
        } catch (Exception e) {
            // GlobalExceptionHandler 에서 처리되도록 변경하거나, 여기서 직접 ApiResponse.fail() 반환
            // 여기서는 일단 예외를 그대로 던져서 GlobalExceptionHandler가 처리하도록 둡니다.
            // 더 구체적인 에러 응답을 원하면 여기서 ApiResponse.fail()을 사용할 수 있습니다.
            // return ResponseEntity.internalServerError().body(ApiResponse.fail("Upload failed: " + e.getMessage()));
            throw new RuntimeException("Upload failed: " + e.getMessage(), e); // GlobalExceptionHandler에서 처리
        }
    }

    @PutMapping("/{contractId}")
    public ResponseEntity<ApiResponse<ContractEntity>> updateContract(
            @PathVariable Long contractId,
            @RequestPart("data") ContractUpdateRequestDTO request, // ContractUpdateRequestDTO 사용
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal String uuid
    ) {
        try {
            UserEntity user = authService.findByUuid(uuid);
            ContractEntity updatedContract = contractService.updateContract(contractId, request, user, file);
            return ResponseEntity.ok(ApiResponse.success(updatedContract));
        } catch (Exception e) {
            // GlobalExceptionHandler 에서 처리
            // return ResponseEntity.internalServerError().body(ApiResponse.fail("Update failed: " + e.getMessage()));
            throw new RuntimeException("Update failed: " + e.getMessage(), e); // GlobalExceptionHandler에서 처리
        }
    }

    @PostMapping("/{contractId}/participants")
    public ResponseEntity<ApiResponse<ContractPartyEntity>> addParticipant(
            @PathVariable Long contractId,
            @RequestBody AddParticipantRequestDTO request, // JSON 요청 본문으로 받음
            @AuthenticationPrincipal String uuid // 요청을 보낸 사용자 (권한 검사용)
    ) {
        try {
            UserEntity actionRequester = authService.findByUuid(uuid);
            ContractPartyEntity newParty = contractService.addParticipantToContract(contractId, request, actionRequester);
            return ResponseEntity.ok(ApiResponse.success(newParty));
        } catch (Exception e) {
            // GlobalExceptionHandler에서 처리
            throw new RuntimeException("Failed to add participant: " + e.getMessage(), e);
        }
    }
    @GetMapping("/{contractId}/versions/{versionNumber}/verify")
    public ResponseEntity<ApiResponse<ContractIntegrityVerificationDTO>> verifyContractIntegrity(
            @PathVariable Long contractId,
            @PathVariable int versionNumber,
            @AuthenticationPrincipal String userUuid // 요청자 UUID
    ) {
        try {
            UserEntity requester = authService.findByUuid(userUuid);
            ContractIntegrityVerificationDTO verificationResult = contractService.verifyContractIntegrity(contractId, versionNumber, requester);
            return ResponseEntity.ok(ApiResponse.success(verificationResult));
        } catch (Exception e) {
            // GlobalExceptionHandler 에서 CustomException은 적절히 처리됨.
            // RuntimeException으로 감싸서 보내면 500 에러와 함께 GlobalExceptionHandler에서 처리될 수 있음 (메시지 포함)
            // 혹은 여기서 직접 ApiResponse.fail()을 사용하여 상태 코드와 메시지를 명시적으로 지정할 수 있음
            // e.g., if (e instanceof CustomException) { throw (CustomException) e; }
            //       return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("Verification failed: " + e.getMessage()));
            throw new RuntimeException("Verification failed: " + e.getMessage(), e);
        }
    }

    @GetMapping // 또는 @GetMapping("/my") 등 원하는 경로로 설정 가능
    public ResponseEntity<ApiResponse<Page<ContractListDTO>>> getMyContracts(
            @AuthenticationPrincipal String uuid, // 인증된 사용자 UUID
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable // 기본 페이지 크기 10, 생성일 내림차순 정렬
    ) {
        try {
            // AuthService를 사용하여 UserEntity를 찾는 과정이 ContractService 내에 이미 있으므로,
            // 여기서는 uuid만 서비스로 전달합니다.
            Page<ContractListDTO> contracts = contractService.getContractsForUser(uuid, pageable);
            return ResponseEntity.ok(ApiResponse.success(contracts));
        } catch (Exception e) {
            // GlobalExceptionHandler에서 처리하거나, 여기서 직접 ApiResponse.fail() 반환
            // 여기서는 예외를 그대로 던져서 GlobalExceptionHandler가 처리하도록 둡니다.
            // 더 구체적인 에러 응답을 원하면 여기서 ApiResponse.fail()을 사용할 수 있습니다.
            // e.g., return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("Failed to retrieve contracts: " + e.getMessage()));
            throw new RuntimeException("Failed to retrieve contracts: " + e.getMessage(), e);
        }
    }

    @GetMapping("/{contractId}")
    public ResponseEntity<ApiResponse<ContractDetailDTO>> getContractDetails(
            @PathVariable Long contractId,
            @AuthenticationPrincipal String uuid // 인증된 사용자 UUID (권한 검사용)
    ) {
        try {
            ContractDetailDTO contractDetails = contractService.getContractDetails(contractId, uuid);
            return ResponseEntity.ok(ApiResponse.success(contractDetails));
        } catch (Exception e) {
            // GlobalExceptionHandler에서 CustomException은 적절히 처리됨.
            // RuntimeException으로 감싸서 보내면 500 에러와 함께 GlobalExceptionHandler에서 처리될 수 있음 (메시지 포함)
            // 혹은 여기서 직접 ApiResponse.fail()을 사용하여 상태 코드와 메시지를 명시적으로 지정할 수 있음
            // e.g., if (e instanceof CustomException) { throw (CustomException) e; }
            //       return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("Failed to retrieve contract details: " + e.getMessage()));
            throw new RuntimeException("Failed to retrieve contract details: " + e.getMessage(), e);
        }
    }

    // --- 👇 계약서 삭제를 위한 핸들러 추가 ---
    @DeleteMapping("/{contractId}")
    public ResponseEntity<ApiResponse<Void>> deleteContract(
            @PathVariable Long contractId,
            @AuthenticationPrincipal String uuid // 요청자 UUID (권한 검사용)
    ) {
        try {
            UserEntity requester = authService.findByUuid(uuid);
            contractService.deleteContract(contractId, requester); // 서비스 계층에 실제 삭제 로직 호출
            return ResponseEntity.ok(ApiResponse.success(null)); // 성공 시 null 데이터와 함께 응답
        } catch (Exception e) {
            // GlobalExceptionHandler에서 CustomException은 적절히 처리됨.
            // RuntimeException으로 감싸서 보내면 500 에러와 함께 GlobalExceptionHandler에서 처리될 수 있음
            throw new RuntimeException("Failed to delete contract: " + e.getMessage(), e);
        }
    }
}