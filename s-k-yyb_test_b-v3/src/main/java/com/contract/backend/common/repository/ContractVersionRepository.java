package com.contract.backend.common.repository;

import com.contract.backend.common.Entity.ContractEntity;
import com.contract.backend.common.Entity.ContractVersionEntity;
import com.contract.backend.common.Entity.enumm.VersionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContractVersionRepository extends JpaRepository<ContractVersionEntity, Long> {
    List<ContractVersionEntity> findByContract(ContractEntity contract);
    Optional<ContractVersionEntity> findByContractAndVersionNumber(ContractEntity contract, int versionNumber);
    List<ContractVersionEntity> findByStatus(VersionStatus status);
}
