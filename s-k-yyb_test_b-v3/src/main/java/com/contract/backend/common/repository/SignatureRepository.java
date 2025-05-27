package com.contract.backend.common.repository;

import com.contract.backend.common.Entity.ContractVersionEntity;
import com.contract.backend.common.Entity.SignatureEntity;
import com.contract.backend.common.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SignatureRepository extends JpaRepository<SignatureEntity, Long> {
    // 특정 계약 버전에 대한 특정 서명자의 서명이 있는지 확인
    Optional<SignatureEntity> findByContractVersionAndSigner(ContractVersionEntity contractVersion, UserEntity signer);

    // 특정 계약 버전에 대한 모든 서명 목록 조회
    List<SignatureEntity> findAllByContractVersion(ContractVersionEntity contractVersion);
}