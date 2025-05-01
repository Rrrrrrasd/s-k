package com.contract.backend.controller;

import com.contract.backend.common.Entity.CredentialEntity;
import com.contract.backend.common.Entity.UserEntity;
import com.contract.backend.common.dto.LoginRequestDTO;
import com.contract.backend.common.dto.SignupRequestDTO;
import com.contract.backend.common.dto.UserResponseDTO;
import com.contract.backend.common.dto.WebAuthnRegisterVerifyRequestDTO;
import com.contract.backend.common.response.ApiResponse;
import com.contract.backend.service.AuthService;
import com.contract.backend.service.WebAuthnService;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final WebAuthnService webAuthnService;

    public AuthController(AuthService authService, WebAuthnService webAuthnService) {
        this.authService = authService;
        this.webAuthnService = webAuthnService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponseDTO>> signup(@RequestBody SignupRequestDTO request){
        UserResponseDTO user = authService.signup(request);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginRequestDTO request){
        String token = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(token));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> uuidMe() {
        String uuid = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity user = authService.findByUuid(uuid);
        UserResponseDTO response = new UserResponseDTO(user.getId(), user.getUserName(), user.getEmail());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/webauthn/register/options")
    public ResponseEntity<PublicKeyCredentialCreationOptions> getRegisterOptions() {
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        UserEntity user = authService.findByEmail(email);
        PublicKeyCredentialCreationOptions options = webAuthnService.generateRegistrationOptions(user);
        return ResponseEntity.ok(options);
    }

    @PostMapping("/webauthn/register/verify")
    public ResponseEntity<ApiResponse<String>> verifyRegistration(@RequestBody WebAuthnRegisterVerifyRequestDTO request) {
        webAuthnService.verifyRegistration(request.getOptions(), request.getDeviceName());
        return ResponseEntity.ok(ApiResponse.success("WebAuthn registration successful"));
    }

    @GetMapping("/webauthn/login/options")
    public ResponseEntity<AssertionRequest> getLoginOptions(@RequestParam String email) {
        UserEntity user = authService.findByEmail(email);
        AssertionRequest options = webAuthnService.generateLoginOptions(user);
        return ResponseEntity.ok(options);
    }

    @PostMapping("/webauthn/login/verify")
    public ResponseEntity<ApiResponse<String>> verifyLogin(@RequestBody FinishAssertionOptions options) {
        String token = webAuthnService.verifyLogin(options);
        return ResponseEntity.ok(ApiResponse.success(token));
    }

    @DeleteMapping("/webauthn/credential/{credentialId}")
    public ResponseEntity<ApiResponse<String>> deleteCredential(@PathVariable String credentialId) {
        String uuid = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        webAuthnService.deleteCredential(credentialId, uuid);
        return ResponseEntity.ok(ApiResponse.success("Credential 삭제 완료"));
    }

    @GetMapping("/webauthn/credentials")
    public ResponseEntity<ApiResponse<List<CredentialEntity>>> getMyCredentials() {
        String uuid = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<CredentialEntity> credentials = webAuthnService.getCredentialsForUser(uuid);
        return ResponseEntity.ok(ApiResponse.success(credentials));
    }
}
