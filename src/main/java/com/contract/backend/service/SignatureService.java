package com.contract.backend.service;

import com.contract.backend.common.Entity.*;
import com.contract.backend.common.Entity.enumm.ContractStatus;
import com.contract.backend.common.Entity.enumm.PartyRole;
import com.contract.backend.common.Entity.enumm.VersionStatus;
import com.contract.backend.common.dto.BlockchainMetadataDTO; // DTO 임포트
import com.contract.backend.common.exception.CustomException;
import com.contract.backend.common.exception.CustomExceptionEnum;
import com.contract.backend.common.repository.ContractPartyRepository;
import com.contract.backend.common.repository.ContractRepository;
import com.contract.backend.common.repository.ContractVersionRepository;
import com.contract.backend.common.repository.SignatureRepository;
import com.fasterxml.jackson.databind.ObjectMapper; // ObjectMapper 임포트
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // JavaTimeModule 임포트
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets; // StandardCharsets 임포트
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException; // NoSuchAlgorithmException 임포트
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.contract.backend.common.repository.BlockchainRecordRepository;

@Service
public class SignatureService {

    private static final Logger logger = LoggerFactory.getLogger(SignatureService.class);

    private final ContractRepository contractRepository;
    private final ContractVersionRepository contractVersionRepository;
    private final SignatureRepository signatureRepository;
    private final ContractPartyRepository contractPartyRepository;
    private final BlockchainService blockchainService;
    private final BlockchainRecordRepository blockchainRecordRepository;
    private final ObjectMapper objectMapper; // JSON 직렬화용

    public SignatureService(ContractRepository contractRepository,
                            ContractVersionRepository contractVersionRepository,
                            SignatureRepository signatureRepository,
                            ContractPartyRepository contractPartyRepository,
                            BlockchainService blockchainService,
                            BlockchainRecordRepository blockchainRecordRepository,
                            ObjectMapper objectMapper // ObjectMapper 주입
    ) {
        this.contractRepository = contractRepository;
        this.contractVersionRepository = contractVersionRepository;
        this.signatureRepository = signatureRepository;
        this.contractPartyRepository = contractPartyRepository;
        this.blockchainService = blockchainService;
        this.blockchainRecordRepository = blockchainRecordRepository;
        this.objectMapper = objectMapper.copy(); // 원본 ObjectMapper의 설정을 복사하여 사용
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Transactional
    public SignatureEntity signContract(Long contractId, UserEntity signer) throws Exception {
        ContractEntity contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.CONTRACT_NOT_FOUND));

        ContractVersionEntity currentVersion = contract.getCurrentVersion();
        if (currentVersion == null) {
            throw new CustomException(CustomExceptionEnum.VERSION_NOT_FOUND);
        }

        if (contract.getStatus() != ContractStatus.OPEN) {
            throw new CustomException(CustomExceptionEnum.CANNOT_SIGN_CONTRACT);
        }
        if (currentVersion.getStatus() != VersionStatus.PENDING_SIGNATURE) {
            throw new CustomException(CustomExceptionEnum.VERSION_NOT_PENDING_SIGNATURE);
        }

        contractPartyRepository.findByContractAndParty(contract, signer)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.UNAUTHORIZED));

        if (signatureRepository.findByContractVersionAndSigner(currentVersion, signer).isPresent()) {
            throw new CustomException(CustomExceptionEnum.ALREADY_SIGNED);
        }

        String signatureHashValue = generateSimpleSignatureHash(currentVersion.getFileHash(), signer.getUuid());
        SignatureEntity signature = new SignatureEntity(currentVersion, signer, signatureHashValue);
        signatureRepository.save(signature);
        logger.info("사용자 {}가 계약 ID {}의 버전 {}에 서명했습니다.", signer.getUuid(), contractId, currentVersion.getVersionNumber());

        List<ContractPartyEntity> allPartiesInContract = contractPartyRepository.findByContract(contract);
        List<UserEntity> requiredSigners = allPartiesInContract.stream()
                .filter(party -> party.getRole() == PartyRole.INITIATOR || party.getRole() == PartyRole.COUNTERPARTY)
                .map(ContractPartyEntity::getParty)
                .collect(Collectors.toList());

        List<SignatureEntity> signaturesForCurrentVersion = signatureRepository.findAllByContractVersion(currentVersion);
        List<UserEntity> actualSigners = signaturesForCurrentVersion.stream()
                .map(SignatureEntity::getSigner)
                .collect(Collectors.toList());

        boolean allRequiredHaveSigned = true;
        if (requiredSigners.isEmpty() && !actualSigners.isEmpty()) {
            allRequiredHaveSigned = false;
        } else if (requiredSigners.isEmpty()) {
            allRequiredHaveSigned = false; // 필수 서명자 없는 계약은 자동 완료 안됨
        } else {
            for (UserEntity requiredSigner : requiredSigners) {
                if (actualSigners.stream().noneMatch(actual -> actual.getId().equals(requiredSigner.getId()))) {
                    allRequiredHaveSigned = false;
                    break;
                }
            }
        }


        if (allRequiredHaveSigned) {
            logger.info("계약 ID {}의 버전 {}에 대한 모든 필수 서명이 완료되었습니다.", contract.getId(), currentVersion.getVersionNumber());
            currentVersion.setStatus(VersionStatus.SIGNED);
            contractVersionRepository.save(currentVersion);

            contract.setStatus(ContractStatus.CLOSED);
            contract.setUpdatedAt(LocalDateTime.now());
            contract.setUpdatedBy(signer);
            contractRepository.save(contract);
            logger.info("계약 ID {}의 상태가 CLOSED로, 버전 {}의 상태가 SIGNED로 변경되었습니다.", contract.getId(), currentVersion.getId());

            // --- 블록체인 연동 로직 ---
            LocalDateTime finalizedTimestamp = LocalDateTime.now(); // 최종 완료 시점 정의

            List<BlockchainMetadataDTO.SignatureMetadataDTO> signatureMetadataDTOs = signaturesForCurrentVersion.stream()
                    .map(sig -> new BlockchainMetadataDTO.SignatureMetadataDTO(
                            sig.getSigner().getUuid(),
                            sig.getSignatureHash(),
                            sig.getSignedAt()))
                    .collect(Collectors.toList());

            BlockchainMetadataDTO metadataForBlockchain = new BlockchainMetadataDTO(
                    currentVersion.getId(),
                    currentVersion.getFileHash(),
                    contract.getTitle(),
                    contract.getCreatedBy().getUuid(),
                    signatureMetadataDTOs,
                    finalizedTimestamp // 정의된 최종 완료 시점 사용
            );

            String metadataJson = objectMapper.writeValueAsString(metadataForBlockchain);
            String metadataHash = generateSHA256ForString(metadataJson.getBytes(StandardCharsets.UTF_8));

            logger.info("블록체인에 메타데이터 기록을 시작합니다. 계약 버전 ID: {}, 메타데이터 해시: {}", currentVersion.getId(), metadataHash);
            try {
                String txHash = blockchainService.recordContractVersionMetadata(metadataForBlockchain);

                BlockchainRecordEntity blockchainRecord = new BlockchainRecordEntity(currentVersion, metadataHash, txHash);
                blockchainRecordRepository.save(blockchainRecord);
                logger.info("계약 버전 ID {}의 메타데이터가 블록체인에 성공적으로 기록되었습니다. TxHash: {}", currentVersion.getId(), txHash);

            } catch (Exception e) {
                logger.error("계약 버전 ID {}의 블록체인 메타데이터 기록 중 오류 발생: {}", currentVersion.getId(), e.getMessage(), e);
                // 전체 트랜잭션 롤백을 위해 RuntimeException 발생
                throw new RuntimeException("블록체인 기록 실패로 인한 전체 롤백: " + e.getMessage(), e);
            }
        }
        return signature;
    }

    private String generateSimpleSignatureHash(String fileHash, String userUuid) throws Exception {
        String dataToHash = fileHash + ":" + userUuid + ":" + LocalDateTime.now().toString();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // Helper method to generate SHA-256 hash for a string
    private String generateSHA256ForString(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}