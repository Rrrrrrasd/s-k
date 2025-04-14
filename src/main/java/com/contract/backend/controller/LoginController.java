package com.contract.backend.controller;

import com.contract.backend.common.dto.FinishLoginRequestDTO;
import com.contract.backend.common.dto.StartLoginRequestDTO;
import com.contract.backend.common.dto.StartLoginResponseDTO;
import com.contract.backend.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webauthn/login")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/start")
    public ResponseEntity<StartLoginResponseDTO> start(@RequestBody StartLoginRequestDTO request) {
        return ResponseEntity.ok(loginService.startLogin(request.getUsername()));
    }

    @PostMapping("/finish")
    public ResponseEntity<Void> finish(@RequestBody FinishLoginRequestDTO request) {
        loginService.finishLogin(request.getUsername(), request.getCredential());
        return ResponseEntity.ok().build();
    }
}
