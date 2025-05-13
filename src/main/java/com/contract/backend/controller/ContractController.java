package com.contract.backend.controller;

import com.contract.backend.common.Entity.ContractEntity;
import com.contract.backend.common.Entity.UserEntity;
import com.contract.backend.common.dto.ContractUploadRequestDTO;
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
    public ResponseEntity<?> uploadContract(
            @RequestPart("data") ContractUploadRequestDTO request,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal String uuid // 또는 UUID 기반 principal
    ) {
        try {
            UserEntity user = authService.findByUuid(uuid);
            ContractEntity contract = contractService.uploadContract(request, user, file);
            return ResponseEntity.ok("Contract uploaded successfully. ID: " + contract.getId());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }
}
