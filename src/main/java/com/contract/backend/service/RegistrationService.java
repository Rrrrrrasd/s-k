package com.contract.backend.service;

import com.contract.backend.common.dto.StartRegistrationResponseDTO;
import com.contract.backend.common.model.CredentialModel;
import com.contract.backend.common.model.UserModel;
import com.contract.backend.common.repository.CredentialJpaRepository;
import com.contract.backend.common.repository.UserRepository;
import com.contract.backend.common.exception.RegistrationNotInProgressException;
import com.fasterxml.jackson.databind.JsonNode;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.RegistrationFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RelyingParty relyingParty;
    private final UserRepository userRepository;
    private final CredentialJpaRepository credentialRepository;
    private final RedisTemplate<String, PublicKeyCredentialCreationOptions> creationOptionsRedisTemplate;

    public StartRegistrationResponseDTO startRegistration(String username) {
        UUID userHandle = UUID.randomUUID();

        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(userHandle.getMostSignificantBits());
        buffer.putLong(userHandle.getLeastSignificantBits());
        ByteArray userHandleBytes = new ByteArray(buffer.array());

        UserIdentity userIdentity = UserIdentity.builder()
                .name(username)
                .displayName(username)
                .id(userHandleBytes)
                .build();

        PublicKeyCredentialCreationOptions options = relyingParty.startRegistration(
                StartRegistrationOptions.builder()
                        .user(userIdentity)
                        .build()
        );

        if (userRepository.findByUsername(username).isEmpty()) {
            userRepository.save(UserModel.builder()
                    .username(username)
                    .userHandle(userHandle)
                    .build());
        }

        creationOptionsRedisTemplate.opsForValue().set("webauthn:register:" + username, options, Duration.ofMinutes(5));

        return new StartRegistrationResponseDTO(options, username);
    }

    public void finishRegistration(String username, JsonNode credentialJson) {
        PublicKeyCredentialCreationOptions request = creationOptionsRedisTemplate.opsForValue().get("webauthn:register:" + username);

        if (request == null) {
            throw new RegistrationNotInProgressException("No registration in progress for username: " + username);
        }

        try {
            PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkc =
                    PublicKeyCredential.parseRegistrationResponseJson(credentialJson.toString());

            RegistrationResult result = relyingParty.finishRegistration(
                    FinishRegistrationOptions.builder()
                            .request(request)
                            .response(pkc)
                            .build()
            );

            UserModel user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String credentialIdEncoded = Base64.getUrlEncoder().encodeToString(pkc.getId().getBytes());

            if (credentialRepository.findByCredentialId(credentialIdEncoded).isPresent()) {
                throw new IllegalStateException("Credential already registered: " + credentialIdEncoded);
            }

            credentialRepository.save(CredentialModel.builder()
                    .credentialId(credentialIdEncoded)
                    .user(user)
                    .publicKeyCose(Base64.getUrlEncoder().encodeToString(result.getPublicKeyCose().getBytes()))
                    .signatureCount(result.getSignatureCount())
                    .build());

            creationOptionsRedisTemplate.delete("webauthn:register:" + username);

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse registration credential JSON", e);
        } catch (RegistrationFailedException e) {
            throw new RuntimeException("WebAuthn registration validation failed", e);
        }
    }
}
