package com.contract.backend.common.repository;

import com.contract.backend.common.Entity.ContractEntity;
import com.contract.backend.common.Entity.ContractPartyEntity;
import com.contract.backend.common.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContractPartyRepository extends JpaRepository<ContractPartyEntity, Long> {
    List<ContractPartyEntity> findByContract(ContractEntity contract);
    List<ContractPartyEntity> findByParty(UserEntity party);
    Optional<ContractPartyEntity> findByContractAndParty(ContractEntity contract, UserEntity party);
}
