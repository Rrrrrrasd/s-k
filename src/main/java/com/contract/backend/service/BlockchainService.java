package com.contract.backend.service;

import com.contract.backend.common.dto.BlockchainMetadataDTO; // BlockchainMetadataDTO import 추가

public interface BlockchainService {
    /**
     * 계약 버전 메타데이터를 블록체인에 기록합니다.
     *
     * @param metadataDto 기록할 메타데이터 DTO
     * @return 블록체인 트랜잭션 ID
     * @throws Exception 블록체인 연동 중 발생할 수 있는 예외
     */
    String recordContractVersionMetadata(BlockchainMetadataDTO metadataDto) throws Exception;

    /**
     * 블록체인에서 특정 계약 버전의 메타데이터를 조회합니다.
     *
     * @param contractVersionDbId 조회할 계약 버전의 데이터베이스 ID
     * @return 조회된 메타데이터 DTO
     * @throws Exception 블록체인 연동 또는 데이터 조회 중 발생할 수 있는 예외
     */
    BlockchainMetadataDTO getContractMetadataFromBlockchain(Long contractVersionDbId) throws Exception;
}