package com.contract.backend.service;

import com.contract.backend.common.Entity.*;
import com.contract.backend.common.Entity.enumm.ContractStatus;
import com.contract.backend.common.Entity.enumm.PartyRole;
import com.contract.backend.common.Entity.enumm.VersionStatus;
import com.contract.backend.common.exception.CustomException;
import com.contract.backend.common.exception.CustomExceptionEnum;
import com.contract.backend.common.repository.ContractPartyRepository;
import com.contract.backend.common.repository.ContractRepository;
import com.contract.backend.common.repository.ContractVersionRepository;
import com.contract.backend.common.repository.SignatureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// 로깅을 위한 import 추가
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// BlockchainRecordEntity 및 Repository import 추가
import com.contract.backend.common.Entity.BlockchainRecordEntity;
import com.contract.backend.common.repository.BlockchainRecordRepository;

@Service
public class SignatureService {

    private static final Logger logger = LoggerFactory.getLogger(SignatureService.class); // 로거 선언

    private final ContractRepository contractRepository;
    private final ContractVersionRepository contractVersionRepository;
    private final SignatureRepository signatureRepository;
    private final ContractPartyRepository contractPartyRepository;
    private final BlockchainService blockchainService; // BlockchainService 주입
    private final BlockchainRecordRepository blockchainRecordRepository; // BlockchainRecordRepository 주입

    public SignatureService(ContractRepository contractRepository,
                            ContractVersionRepository contractVersionRepository,
                            SignatureRepository signatureRepository,
                            ContractPartyRepository contractPartyRepository,
                            BlockchainService blockchainService, // 생성자에 추가
                            BlockchainRecordRepository blockchainRecordRepository // 생성자에 추가
    ) {
        this.contractRepository = contractRepository;
        this.contractVersionRepository = contractVersionRepository;
        this.signatureRepository = signatureRepository;
        this.contractPartyRepository = contractPartyRepository;
        this.blockchainService = blockchainService; // 주입된 객체 할당
        this.blockchainRecordRepository = blockchainRecordRepository; // 주입된 객체 할당
    }

    @Transactional
    public SignatureEntity signContract(Long contractId, UserEntity signer) throws Exception {
        // 1. 계약 및 현재 버전 조회
        ContractEntity contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.CONTRACT_NOT_FOUND));

        ContractVersionEntity currentVersion = contract.getCurrentVersion();
        if (currentVersion == null) {
            throw new CustomException(CustomExceptionEnum.VERSION_NOT_FOUND);
        }

        // 2. 계약 상태 및 버전 상태 확인
        if (contract.getStatus() != ContractStatus.OPEN) {
            throw new CustomException(CustomExceptionEnum.CANNOT_SIGN_CONTRACT);
        }
        if (currentVersion.getStatus() != VersionStatus.PENDING_SIGNATURE) {
            throw new CustomException(CustomExceptionEnum.VERSION_NOT_PENDING_SIGNATURE);
        }

        // 3. 서명 권한 확인
        ContractPartyEntity contractParty = contractPartyRepository.findByContractAndParty(contract, signer)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.UNAUTHORIZED));

        // 4. 중복 서명 확인
        Optional<SignatureEntity> existingSignature = signatureRepository.findByContractVersionAndSigner(currentVersion, signer);
        if (existingSignature.isPresent()) {
            throw new CustomException(CustomExceptionEnum.ALREADY_SIGNED);
        }

        // 5. SignatureEntity 생성 및 저장
        String signatureHashValue = generateSimpleSignatureHash(currentVersion.getFileHash(), signer.getUuid());
        SignatureEntity signature = new SignatureEntity(currentVersion, signer, signatureHashValue);
        signatureRepository.save(signature);
        logger.info("사용자 {}가 계약 ID {}의 버전 {}에 서명했습니다.", signer.getUuid(), contractId, currentVersion.getVersionNumber());


        // 6. 모든 필수 참여자 서명 완료 여부 확인
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
        if (requiredSigners.isEmpty() && !actualSigners.isEmpty()) { // 필수 서명자는 없는데 누군가 서명한 경우 (일반적이지 않음)
            allRequiredHaveSigned = false;
        } else if (requiredSigners.isEmpty()) { // 필수 서명자가 없는 계약의 경우 (정책에 따라 바로 SIGNED 처리 가능)
            allRequiredHaveSigned = false; // 여기서는 필수 서명자가 있어야 한다고 가정
        }


        for (UserEntity requiredSigner : requiredSigners) {
            if (actualSigners.stream().noneMatch(actual -> actual.getId().equals(requiredSigner.getId()))) {
                allRequiredHaveSigned = false;
                break;
            }
        }

        if (allRequiredHaveSigned && !requiredSigners.isEmpty()) {
            logger.info("계약 ID {}의 버전 {}에 대한 모든 필수 서명이 완료되었습니다.", contract.getId(), currentVersion.getVersionNumber());
            currentVersion.setStatus(VersionStatus.SIGNED);
            contractVersionRepository.save(currentVersion);

            contract.setStatus(ContractStatus.CLOSED);
            contract.setUpdatedAt(LocalDateTime.now());
            contract.setUpdatedBy(signer); // 마지막 서명자를 updatedBy로 설정
            contractRepository.save(contract);
            logger.info("계약 ID {}의 상태가 CLOSED로, 버전 {}의 상태가 SIGNED로 변경되었습니다.", contract.getId(), currentVersion.getId());


            // <<< 블록체인 연동 로직 시작 >>>
            logger.info("블록체인에 메타데이터 기록을 시작합니다. 계약 버전 ID: {}", currentVersion.getId());
            try {
                // 블록체인에 메타데이터 기록 (실제 서명자 목록 전달)
                String txHash = blockchainService.recordContractVersionMetadata(currentVersion, actualSigners);

                // 블록체인 기록 정보 DB에 저장
                // metadataHash는 블록체인에 기록한 메타데이터 자체의 해시값 또는 대표값을 사용합니다.
                // 여기서는 예시로 currentVersion의 fileHash를 사용하지만,
                // 실제로는 blockchainService에서 반환하거나 생성한 값을 사용하는 것이 더 정확할 수 있습니다.
                String metadataHashForRecord = currentVersion.getFileHash(); // 또는 전송한 JSON 데이터의 해시
                BlockchainRecordEntity blockchainRecord = new BlockchainRecordEntity(currentVersion, metadataHashForRecord, txHash);
                blockchainRecordRepository.save(blockchainRecord);
                logger.info("계약 버전 ID {}의 메타데이터가 블록체인에 성공적으로 기록되었습니다. TxHash: {}", currentVersion.getId(), txHash);

            } catch (Exception e) {
                logger.error("계약 버전 ID {}의 블록체인 메타데이터 기록 중 오류 발생: {}", currentVersion.getId(), e.getMessage(), e);
                // 블록체인 기록 실패 시 처리 정책 결정 필요:
                // 1. 오류 로깅 후 계속 진행 (DB 상태는 SIGNED/CLOSED 유지)
                // 2. RuntimeException을 발생시켜 전체 트랜잭션 롤백 (DB 상태는 이전으로 복귀)
                //    throw new RuntimeException("블록체인 기록 실패로 인한 전체 롤백: " + e.getMessage(), e);
                // 3. 별도 상태(예: PENDING_BLOCKCHAIN_CONFIRMATION)로 계약 상태 변경 후, 재시도 로직 구현
                // 현재는 로깅만 하고 넘어갑니다. 필요에 따라 주석 처리된 throw new RuntimeException(...)을 활성화하여 롤백을 유도할 수 있습니다.
            }
            // <<< 블록체인 연동 로직 종료 >>>
        }

        return signature;
    }

    // 예시용 간단한 서명 해시 생성 메소드
    private String generateSimpleSignatureHash(String fileHash, String userUuid) throws Exception {
        String dataToHash = fileHash + ":" + userUuid + ":" + LocalDateTime.now().toString();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(dataToHash.getBytes("UTF-8"));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}