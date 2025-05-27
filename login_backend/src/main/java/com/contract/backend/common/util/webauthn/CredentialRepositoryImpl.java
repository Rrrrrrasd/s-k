package com.contract.backend.common.util.webauthn;

import com.contract.backend.common.Entity.CredentialEntity;
import com.contract.backend.common.repository.JpaCredentialRepository;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.PublicKeyCredentialType;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CredentialRepositoryImpl implements CredentialRepository {

    private final JpaCredentialRepository credentialRepository;

    public CredentialRepositoryImpl(JpaCredentialRepository credentialRepository) {
        this.credentialRepository = credentialRepository;
    }

    // username = userHandle = UUID
    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        List<CredentialEntity> credentials = credentialRepository.findAllByUserHandle(username);
        return credentials.stream()
                .map(cred -> PublicKeyCredentialDescriptor.builder()
                        .id(new ByteArray(Base64.getUrlDecoder().decode(cred.getCredentialId())))
                        .type(PublicKeyCredentialType.PUBLIC_KEY)
                        .build())
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        return credentialRepository.findByUserHandle(username)
                .map(cred -> new ByteArray(cred.getUserHandle().getBytes()));
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        return credentialRepository.findByUserHandle(new String(userHandle.getBytes()))
                .map(CredentialEntity::getUserHandle);
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        return credentialRepository.findByCredentialId(credentialId.getBase64Url())
                .map(cred -> RegisteredCredential.builder()
                        .credentialId(credentialId)
                        .userHandle(new ByteArray(cred.getUserHandle().getBytes()))
                        .publicKeyCose(new ByteArray(Base64.getUrlDecoder().decode(cred.getPublicKeyCose())))
                        .signatureCount(cred.getSignatureCount())
                        .build());
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        return credentialRepository.findAllByCredentialId(credentialId.getBase64Url())
                .stream()
                .map(cred -> RegisteredCredential.builder()
                        .credentialId(credentialId)
                        .userHandle(new ByteArray(cred.getUserHandle().getBytes()))
                        .publicKeyCose(new ByteArray(Base64.getUrlDecoder().decode(cred.getPublicKeyCose())))
                        .signatureCount(cred.getSignatureCount())
                        .build())
                .collect(Collectors.toSet());
    }
}

