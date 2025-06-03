package com.contract.backend.common.repository;

import com.contract.backend.common.Entity.BlockchainRecordEntity;
import com.contract.backend.common.Entity.ContractVersionEntity; // ContractVersionEntity import 추가
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlockchainRecordRepository extends JpaRepository<BlockchainRecordEntity, Long> {

    // ContractVersionEntity 객체로 BlockchainRecordEntity 조회
    Optional<BlockchainRecordEntity> findByContractVersion(ContractVersionEntity contractVersion);

    // 필요한 경우 ContractVersionEntity의 ID로 조회하는 메소드도 추가할 수 있습니다.
    // Optional<BlockchainRecordEntity> findByContractVersion_Id(Long contractVersionId);
}