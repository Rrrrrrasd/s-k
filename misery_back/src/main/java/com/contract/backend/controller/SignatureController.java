package com.contract.backend.controller;

import com.contract.backend.common.Entity.SignatureEntity;
import com.contract.backend.common.Entity.UserEntity;
import com.contract.backend.common.response.ApiResponse;
import com.contract.backend.service.AuthService;
import com.contract.backend.service.SignatureService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contracts/{contractId}/sign") // 경로를 이렇게 구성
public class SignatureController {

    private final SignatureService signatureService;
    private final AuthService authService;

    public SignatureController(SignatureService signatureService, AuthService authService) {
        this.signatureService = signatureService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SignatureEntity>> signContract(
            @PathVariable Long contractId,
            @AuthenticationPrincipal String userUuid) { // JWT 토큰에서 사용자 UUID 가져옴
        try {
            UserEntity signer = authService.findByUuid(userUuid);
            SignatureEntity signature = signatureService.signContract(contractId, signer);
            return ResponseEntity.ok(ApiResponse.success(signature));
        } catch (Exception e) {
            // GlobalExceptionHandler 에서 처리
            throw new RuntimeException("Failed to sign contract: " + e.getMessage(), e);
        }
    }
}