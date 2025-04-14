package com.contract.backend.common.util.webauthn;

import com.contract.backend.common.model.UserModel;
import com.contract.backend.common.repository.CredentialJpaRepository;
import com.contract.backend.common.repository.UserRepository;
import com.contract.backend.common.model.CredentialModel;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WebAuthnCredentialRepository implements CredentialRepository {

    private final UserRepository userRepository;
    private final CredentialJpaRepository credentialRepository;

    private ByteArray uuidToByteArray(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return new ByteArray(buffer.array());
    }

    private UUID byteArrayToUUID(ByteArray byteArray) {
        ByteBuffer buffer = ByteBuffer.wrap(byteArray.getBytes());
        return new UUID(buffer.getLong(), buffer.getLong());
    }

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        return userRepository.findByUsername(username)
                .map(user -> user.getCredentials().stream()
                        .map(cred -> {
                            try {
                                return PublicKeyCredentialDescriptor.builder()
                                        .id(new ByteArray(Base64.getUrlDecoder().decode(cred.getCredentialId())))
                                        .build();
                            } catch (IllegalArgumentException e) {
                                return null; // 오류 무시하고 필터링
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        return userRepository.findByUsername(username)
                .map(user -> uuidToByteArray(user.getUserHandle()));
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        UUID handle = byteArrayToUUID(userHandle);
        return userRepository.findByUserHandle(handle)
                .map(UserModel::getUsername);
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        UUID handle = byteArrayToUUID(userHandle);

        return userRepository.findByUserHandle(handle)
                .flatMap(user -> user.getCredentials().stream()
                        .filter(cred -> Arrays.equals(Base64.getUrlDecoder().decode(cred.getCredentialId()), credentialId.getBytes()))
                        .findFirst()
                        .map(cred -> RegisteredCredential.builder()
                                .credentialId(credentialId)
                                .userHandle(userHandle)
                                .publicKeyCose(new ByteArray(Base64.getUrlDecoder().decode(cred.getPublicKeyCose())))
                                .signatureCount(cred.getSignatureCount())
                                .build())
                );
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        return credentialRepository.findByCredentialId(
                Base64.getUrlEncoder().encodeToString(credentialId.getBytes())
        ).map(cred -> Set.of(
                RegisteredCredential.builder()
                        .credentialId(credentialId)
                        .userHandle(uuidToByteArray(cred.getUser().getUserHandle()))
                        .publicKeyCose(new ByteArray(Base64.getUrlDecoder().decode(cred.getPublicKeyCose())))
                        .signatureCount(cred.getSignatureCount())
                        .build()
        )).orElse(Collections.emptySet());
    }
}
