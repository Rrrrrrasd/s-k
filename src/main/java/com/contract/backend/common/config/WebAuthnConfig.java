package com.contract.backend.common.config;

import com.contract.backend.common.repository.CredentialJpaRepository;
import com.contract.backend.common.repository.UserRepository;
import com.contract.backend.common.util.webauthn.WebAuthnCredentialRepository;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class WebAuthnConfig {

    private final UserRepository userRepository;
    private final CredentialJpaRepository credentialRepository;

    @Bean
    public RelyingParty relyingParty(WebAuthnCredentialRepository credentialRepoAdapter) {
        return RelyingParty.builder()
                .identity(
                        RelyingPartyIdentity.builder()
                                .id("localhost")
                                .name("My WebAuthn App")
                                .build()
                )
                .credentialRepository(credentialRepoAdapter)
                .origins(Set.of("http://localhost:8080"))
                .allowUntrustedAttestation(true)
                .build();
    }
}