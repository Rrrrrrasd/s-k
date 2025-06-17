package com.contract.backend.common.repository;

import com.contract.backend.common.Entity.CredentialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaCredentialRepository extends JpaRepository<CredentialEntity, Long> {
    Optional<CredentialEntity> findByCredentialId(String credentialId);
    List<CredentialEntity> findAllByCredentialId(String credentialId);

    List<CredentialEntity> findAllByUser_Uuid(String uuid);

    //삭제
    //Optional<CredentialEntity> findByUserHandle(String userHandle);

    Optional<CredentialEntity> findByCredentialIdAndUser_Uuid(String credentialId, String uuid);
    List<CredentialEntity> findAllByUser_UuidOrderByCreatedAtDesc(String uuid);
}
