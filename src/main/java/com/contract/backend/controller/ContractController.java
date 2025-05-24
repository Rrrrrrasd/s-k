package com.contract.backend.controller;

import com.contract.backend.common.Entity.ContractEntity;
import com.contract.backend.common.Entity.ContractPartyEntity;
import com.contract.backend.common.Entity.UserEntity;
import com.contract.backend.common.dto.AddParticipantRequestDTO;
import com.contract.backend.common.dto.ContractUploadRequestDTO;
import com.contract.backend.common.dto.ContractUpdateRequestDTO; // 추가
import com.contract.backend.common.response.ApiResponse; // 추가 (ApiResponse 사용을 위해)
import com.contract.backend.service.AuthService;
import com.contract.backend.service.ContractService;

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
}