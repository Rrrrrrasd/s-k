package com.contract.backend.service;

import com.contract.backend.common.dto.StartLoginResponseDTO;
import com.contract.backend.common.model.UserModel;
import com.contract.backend.common.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.AssertionFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final RelyingParty relyingParty;
    private final UserRepository userRepository;
    private final RedisTemplate<String, PublicKeyCredentialRequestOptions> requestOptionsRedisTemplate;

    // ✅ 로그인 시작: challenge 생성 + credential 정보 반환
    public StartLoginResponseDTO startLogin(String username) {
        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(user.getUserHandle().getMostSignificantBits());
        buffer.putLong(user.getUserHandle().getLeastSignificantBits());
        ByteArray userHandle = new ByteArray(buffer.array());

        AssertionRequest request = relyingParty.startAssertion(
                StartAssertionOptions.builder()
                        .username(Optional.of(username))
                        .build()
        );

        // Redis 캐시에 저장
        requestOptionsRedisTemplate.opsForValue().set("webauthn:login:" + username, request.getPublicKeyCredentialRequestOptions(), Duration.ofMinutes(5));

        return new StartLoginResponseDTO(request.getPublicKeyCredentialRequestOptions(), username);
    }

    // ✅ 로그인 완료: 클라이언트 응답 검증
    public void finishLogin(String username, JsonNode credentialJson) {
        PublicKeyCredentialRequestOptions request =
                requestOptionsRedisTemplate.opsForValue().get("webauthn:login:" + username);

        if (request == null) {
            throw new IllegalStateException("No login in progress for user: " + username);
        }

        try {
            PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> pkc =
                    PublicKeyCredential.parseAssertionResponseJson(credentialJson.toString());

            AssertionRequest assertionRequest = AssertionRequest.builder()
                    .publicKeyCredentialRequestOptions(request)
                    .username(Optional.of(username))
                    .build();

            AssertionResult result = relyingParty.finishAssertion(
                    FinishAssertionOptions.builder()
                            .request(assertionRequest)
                            .response(pkc)
                            .build()
            );

            if (!result.isSuccess()) {
                throw new IllegalStateException("Login failed: Assertion result is not successful");
            }

            requestOptionsRedisTemplate.delete("webauthn:login:" + username);

        } catch (IOException | AssertionFailedException e) {
            throw new RuntimeException("Login verification failed", e);
        }
    }
}