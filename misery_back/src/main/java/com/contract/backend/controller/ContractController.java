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
            return ResponseEntity.ok(ApiResponse.success(contract)); 
        } catch (Exception e) {
            
            throw new RuntimeException("Upload failed: " + e.getMessage(), e); 
        }
    }

    @PutMapping("/{contractId}")
    public ResponseEntity<ApiResponse<ContractEntity>> updateContract(
            @PathVariable Long contractId,
            @RequestPart("data") ContractUpdateRequestDTO request, 
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal String uuid
    ) {
        try {
            UserEntity user = authService.findByUuid(uuid);
            ContractEntity updatedContract = contractService.updateContract(contractId, request, user, file);
            return ResponseEntity.ok(ApiResponse.success(updatedContract));
        } catch (Exception e) {
            
            throw new RuntimeException("Update failed: " + e.getMessage(), e); 
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
            
            throw new RuntimeException("Verification failed: " + e.getMessage(), e);
        }
    }

    @GetMapping 
    public ResponseEntity<ApiResponse<Page<ContractListDTO>>> getMyContracts(
            @AuthenticationPrincipal String uuid, 
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable // 기본 페이지 크기 10, 생성일 내림차순 정렬
    ) {
        try {
            
            Page<ContractListDTO> contracts = contractService.getContractsForUser(uuid, pageable);
            return ResponseEntity.ok(ApiResponse.success(contracts));
        } catch (Exception e) {
            
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
           
            throw new RuntimeException("Failed to retrieve contract details: " + e.getMessage(), e);
        }
    }

    //계약서 삭제를 위한 핸들러 추가 
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
            throw new RuntimeException("Failed to delete contract: " + e.getMessage(), e);
        }
    }
}
