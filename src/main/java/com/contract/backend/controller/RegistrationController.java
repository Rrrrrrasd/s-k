package com.contract.backend.controller;

import com.contract.backend.common.dto.FinishRegistrationRequestDTO;
import com.contract.backend.common.dto.StartRegistrationRequestDTO;
import com.contract.backend.common.dto.StartRegistrationResponseDTO;
import com.contract.backend.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/webauthn/register")
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/start")
    public ResponseEntity<StartRegistrationResponseDTO> start(@RequestBody StartRegistrationRequestDTO request) {
        return ResponseEntity.ok(registrationService.startRegistration(request.getUsername()));
    }

    @PostMapping("/finish")
    public ResponseEntity<Void> finish(@RequestBody FinishRegistrationRequestDTO request) {
        registrationService.finishRegistration(request.getUsername(), request.getCredential());
        return ResponseEntity.ok().build();
    }
}
