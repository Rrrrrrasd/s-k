package com.contract.backend.common.repository;

import com.contract.backend.common.Entity.ContractEntity;
import com.contract.backend.common.Entity.UserEntity;
import com.contract.backend.common.Entity.enumm.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractRepository extends JpaRepository<ContractEntity, Long> {
    List<ContractEntity> findByCreatedBy(UserEntity user);
    List<ContractEntity> findByStatus(ContractStatus status);
}
