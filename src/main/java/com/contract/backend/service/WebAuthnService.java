package com.contract.backend.service;

import com.contract.backend.common.Entity.CredentialEntity;
import com.contract.backend.common.Entity.UserEntity;
import com.contract.backend.common.dto.AuthResponseDTO;
import com.contract.backend.common.exception.CustomException;
import com.contract.backend.common.exception.CustomExceptionEnum;
import com.contract.backend.common.repository.JpaCredentialRepository;
import com.contract.backend.common.repository.UserRepository;
import com.contract.backend.common.util.jwt.JwtTokenProvider;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import org.springframework.stereotype.Service;
import java.time.Clock;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class WebAuthnService {

    private final RelyingParty relyingParty;
    private final JpaCredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public WebAuthnService(CredentialRepository credentialRepository,
                           JpaCredentialRepository credentialJpaRepository,
                           UserRepository userRepository,
                           JwtTokenProvider jwtTokenProvider) {
        this.credentialRepository = credentialJpaRepository;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.relyingParty = RelyingParty.builder()
                .identity(RelyingPartyIdentity.builder()
                        .id("localhost")
                        .name("Contract Platform")
                        .build())
                .credentialRepository(credentialRepository)
                .origins(Collections.singleton("http://localhost:5173"))
                .attestationConveyancePreference(AttestationConveyancePreference.NONE)
                .allowUntrustedAttestation(true) // local 환경에선 true  실제 실행시 false
                .clock(Clock.systemUTC())
                .build();
    }

    public PublicKeyCredentialCreationOptions generateRegistrationOptions(UserEntity user) {
        return relyingParty.startRegistration(
                StartRegistrationOptions.builder()
                        .user(UserIdentity.builder()
                                .name(user.getUuid())
                                .displayName(user.getUserName())
                                .id(new ByteArray(user.getUuid().getBytes()))
                                .build())
                        .build()
        );
    }

    public void verifyRegistration(FinishRegistrationOptions options, String deviceName) {
        try {
            RegistrationResult result = relyingParty.finishRegistration(options);

            String credentialId = result.getKeyId().getId().getBase64Url();
            String userHandle = new String(options.getRequest().getUser().getId().getBytes());

            // 중복 Credential 검사
            if (credentialRepository.findByCredentialIdAndUserHandle(credentialId, userHandle).isPresent()) {
                throw new CustomException(CustomExceptionEnum.CREDENTIAL_ALREADY_EXISTS);
            }

            String publicKeyCose = result.getPublicKeyCose().getBase64Url();
            long signatureCount = result.getSignatureCount();

            CredentialEntity credential = new CredentialEntity(credentialId, userHandle, publicKeyCose, signatureCount, deviceName);
            credentialRepository.save(credential);
        } catch (RegistrationFailedException e) {
            throw new CustomException(CustomExceptionEnum.WEBAUTHN_REGISTRATION_FAILED);
        }
    }

    public AssertionRequest generateLoginOptions(UserEntity user) {
        return relyingParty.startAssertion(
                StartAssertionOptions.builder()
                        .username(user.getUuid())
                        .userVerification(UserVerificationRequirement.PREFERRED)
                        .build()
        );
    }

    public AuthResponseDTO verifyLogin(FinishAssertionOptions options) {
        try {
            AssertionResult result = relyingParty.finishAssertion(options);

            if (!result.isSuccess()) {
                throw new CustomException(CustomExceptionEnum.WEBAUTHN_AUTHENTICATION_FAILED);
            }

            // 인증된 사용자 UUID를 기반으로 사용자 조회 후 토큰 발급
            String userHandle = new String(result.getUserHandle().getBytes());
            UserEntity user = userRepository.findByUuid(userHandle)
                    .orElseThrow(() -> new CustomException(CustomExceptionEnum.WEBAUTHN_AUTHENTICATION_FAILED));


            //다수 Credential 중에서 정확히 일치하는 credentialId를 찾는 방식
            String credentialId = result.getCredentialId().getBase64Url(); // 제출된 credential ID
            List<CredentialEntity> credentials = credentialRepository.findAllByUserHandle(userHandle); // 유저의 모든 Credential 조회

            CredentialEntity credential = credentials.stream()
                    .filter(c -> c.getCredentialId().equals(credentialId)) // 정확히 제출된 credentialId와 일치하는 것만 선택
                    .findFirst()
                    .orElseThrow(() -> new CustomException(CustomExceptionEnum.WEBAUTHN_AUTHENTICATION_FAILED));

            if (!credential.isActive()) {
                throw new CustomException(CustomExceptionEnum.WEBAUTHN_AUTHENTICATION_FAILED);
            }

            // signatureCount 비교 후 업데이트(리플레이 공격 방지)
            if (result.getSignatureCount() > credential.getSignatureCount()) {
                credential.setSignatureCount(result.getSignatureCount());
                credential.setLastUsedAt(LocalDateTime.now());
                credentialRepository.save(credential);
            } else {
                throw new CustomException(CustomExceptionEnum.WEBAUTHN_AUTHENTICATION_FAILED);
            }

            String accessToken  = jwtTokenProvider.createToken(user.getUuid());
            String refreshToken = jwtTokenProvider.createRefreshToken(user.getUuid());
            return new AuthResponseDTO(accessToken, refreshToken);
        } catch (AssertionFailedException e) {
            throw new CustomException(CustomExceptionEnum.WEBAUTHN_AUTHENTICATION_FAILED);
        }
    }

    public void deleteCredential(String credentialId, String requesterUuid) {
        CredentialEntity credential = credentialRepository.findByCredentialId(credentialId)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.CREDENTIAL_NOT_FOUND));

        if (!credential.getUserHandle().equals(requesterUuid)) {
            throw new CustomException(CustomExceptionEnum.UNAUTHORIZED); // ❗예외 추가 필요
        }
        credentialRepository.delete(credential);
    }

    public List<CredentialEntity> getCredentialsForUser(String uuid) {
        return credentialRepository.findAllByUserHandleOrderByCreatedAtDesc(uuid);
    }

    //Credential 비활성화 추가할지 안할진 모름
    public void deactivateCredential(String credentialId, String requesterUuid) {
        CredentialEntity credential = credentialRepository.findByCredentialId(credentialId)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.CREDENTIAL_NOT_FOUND));

        if (!credential.getUserHandle().equals(requesterUuid)) {
            throw new CustomException(CustomExceptionEnum.UNAUTHORIZED);
        }

        credential.setActive(false);
        credentialRepository.save(credential);
    }

}

