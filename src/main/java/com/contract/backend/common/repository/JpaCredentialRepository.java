package com.contract.backend.common.repository;

import com.contract.backend.common.Entity.CredentialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaCredentialRepository extends JpaRepository<CredentialEntity, Long> {
    Optional<CredentialEntity> findByCredentialId(String credentialId);
    List<CredentialEntity> findAllByCredentialId(String credentialId);
    List<CredentialEntity> findAllByUserHandle(String userHandle);
    Optional<CredentialEntity> findByUserHandle(String userHandle);
}
