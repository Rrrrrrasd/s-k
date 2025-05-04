package com.contract.backend.controller;

import com.contract.backend.common.Entity.CredentialEntity;
import com.contract.backend.common.Entity.UserEntity;
import com.contract.backend.common.dto.*;
import com.contract.backend.common.exception.CustomException;
import com.contract.backend.common.exception.CustomExceptionEnum;
import com.contract.backend.common.response.ApiResponse;
import com.contract.backend.common.util.jwt.JwtTokenProvider;
import com.contract.backend.service.AuthService;
import com.contract.backend.service.WebAuthnService;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final WebAuthnService webAuthnService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(
            AuthService authService,
            WebAuthnService webAuthnService,
            JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.webAuthnService = webAuthnService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponseDTO>> signup(@RequestBody SignupRequestDTO request){
        UserResponseDTO user = authService.signup(request);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@RequestBody LoginRequestDTO request, HttpServletResponse response){
        AuthResponseDTO tokens = authService.login(request);

        // HttpOnly, Secure 쿠키로 refreshToken 설정
        ResponseCookie cookie = jwtTokenProvider.createRefreshTokenCookie(tokens.getRefreshToken());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(
                ApiResponse.success(new AuthResponseDTO(tokens.getAccessToken(), null))
        );
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
        String uuid = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity user = authService.findByUuid(uuid);
        PublicKeyCredentialCreationOptions options = webAuthnService.generateRegistrationOptions(user);
        return ResponseEntity.ok(options);
    }

    @PostMapping("/webauthn/register/verify")
    public ResponseEntity<ApiResponse<Void>> verifyRegister(@RequestBody WebAuthnRegisterVerifyRequestDTO requestDto) {

        FinishRegistrationOptions options = FinishRegistrationOptions.builder()
                .request(requestDto.getRequest())
                .response(requestDto.getResponse())
                .build();

        webAuthnService.verifyRegistration(options, requestDto.getDeviceName());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/webauthn/login/options")
    public ResponseEntity<AssertionRequest> getLoginOptions(@RequestParam String email) {
        UserEntity user = authService.findByEmail(email);
        AssertionRequest options = webAuthnService.generateLoginOptions(user);
        return ResponseEntity.ok(options);
    }
    @PostMapping("/webauthn/login/verify")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> verifyLogin(
           @RequestBody WebAuthnLoginVerifyRequestDTO dto,
           HttpServletResponse response       // 쿠키 쓰기 위해
    ){
        FinishAssertionOptions options = FinishAssertionOptions.builder()
                .request(dto.getRequest())
                .response(dto.getResponse())
                .build();
        AuthResponseDTO tokens = webAuthnService.verifyLogin(options);

        // — 로그인과 동일하게 Refresh Token을 HttpOnly Cookie에 담아 응답 헤더로 추가
        ResponseCookie cookie = jwtTokenProvider.createRefreshTokenCookie(tokens.getRefreshToken());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        // body에는 Access Token만 내려줍니다.
        return ResponseEntity.ok(
                ApiResponse.success(new AuthResponseDTO(tokens.getAccessToken(), null))
        );
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

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponseDTO>> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie
    ) {
        if (refreshTokenCookie == null ||
                !jwtTokenProvider.validateToken(refreshTokenCookie)) {
            throw new CustomException(CustomExceptionEnum.UNAUTHORIZED);
        }

        String userUuid       = jwtTokenProvider.getUserUuid(refreshTokenCookie);
        String newAccessToken = jwtTokenProvider.createToken(userUuid);

        return ResponseEntity.ok(
                ApiResponse.success(new TokenResponseDTO(newAccessToken))
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
