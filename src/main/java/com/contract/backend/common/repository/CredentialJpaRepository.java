package com.contract.backend.common.repository;

import com.contract.backend.common.model.CredentialModel;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CredentialJpaRepository extends JpaRepository<CredentialModel, Long> {
    Optional<CredentialModel> findByCredentialId(String credentialId);
}
