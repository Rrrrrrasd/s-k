package com.contract.backend.service;

import com.contract.backend.common.Entity.ContractVersionEntity;
import com.contract.backend.common.Entity.UserEntity;
import com.contract.backend.common.dto.BlockchainMetadataDTO; // BlockchainMetadataDTO import 추가

import java.util.List;

public interface BlockchainService {
    /**
     * 계약 버전 메타데이터를 블록체인에 기록합니다.
     *
     * @param contractVersion 기록할 계약 버전 엔티티
     * @param signers 최종 서명자 목록 (또는 관련 정보)
     * @return 블록체인 트랜잭션 ID 또는 기록된 데이터의 ID
     * @throws Exception 블록체인 연동 중 발생할 수 있는 예외
     */
    String recordContractVersionMetadata(ContractVersionEntity contractVersion, List<UserEntity> signers) throws Exception;

    /**
     * 블록체인에서 특정 계약 버전의 메타데이터를 조회합니다.
     *
     * @param contractVersionDbId 조회할 계약 버전의 데이터베이스 ID
     * @return 조회된 메타데이터 DTO
     * @throws Exception 블록체인 연동 또는 데이터 조회 중 발생할 수 있는 예외
     */
    BlockchainMetadataDTO getContractMetadataFromBlockchain(Long contractVersionDbId) throws Exception; // 메소드 선언 추가
}