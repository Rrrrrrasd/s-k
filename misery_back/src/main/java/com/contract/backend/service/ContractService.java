package com.contract.backend.service;

import com.contract.backend.common.Entity.*;
import com.contract.backend.common.Entity.enumm.ContractStatus;
import com.contract.backend.common.Entity.enumm.PartyRole;
import com.contract.backend.common.Entity.enumm.VersionStatus;
import com.contract.backend.common.dto.*;
import com.contract.backend.common.exception.CustomException;
import com.contract.backend.common.exception.CustomExceptionEnum;
import com.contract.backend.common.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContractService {

    private static final Logger logger = LoggerFactory.getLogger(ContractService.class);

    private final ContractRepository contractRepository;
    private final ContractVersionRepository contractVersionRepository;
    private final ContractPartyRepository contractPartyRepository;
    private final S3StorageService s3StorageService;
    private final UserRepository userRepository;
    private final SignatureRepository signatureRepository;
    private final BlockchainRecordRepository blockchainRecordRepository;
    private final BlockchainService blockchainService;
    private final ObjectMapper objectMapper;


    public ContractService(
            ContractRepository contractRepository,
            ContractVersionRepository contractVersionRepository,
            ContractPartyRepository contractPartyRepository,
            S3StorageService s3StorageService,
            UserRepository userRepository,
            SignatureRepository signatureRepository,
            BlockchainRecordRepository blockchainRecordRepository,
            @Qualifier("blockchainService") BlockchainService blockchainService) {
        this.contractRepository = contractRepository;
        this.contractVersionRepository = contractVersionRepository;
        this.contractPartyRepository = contractPartyRepository;
        this.s3StorageService = s3StorageService;
        this.userRepository = userRepository;
        this.signatureRepository = signatureRepository;
        this.blockchainRecordRepository = blockchainRecordRepository;
        this.blockchainService = blockchainService;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Transactional
    public ContractEntity uploadContract(
            ContractUploadRequestDTO request,
            UserEntity uploader,
            MultipartFile file
    ) throws Exception {
        ContractEntity contract = new ContractEntity(
                request.getTitle(),
                request.getDescription(),
                uploader,
                ContractStatus.OPEN
        );
        contract = contractRepository.save(contract);

        String fileHash = generateSHA256FromFile(file.getBytes());
        String filePath = s3StorageService.upload(file);
        String bucket = s3StorageService.getBucketName();

        ContractVersionEntity version = new ContractVersionEntity(
                contract,
                1,
                filePath,
                fileHash,
                VersionStatus.PENDING_SIGNATURE
        );
        version.setBucketName(bucket);
        version.setStorageProvider("B2");
        contractVersionRepository.save(version);

        contract.setCurrentVersion(version);
        contractRepository.save(contract);

        contractPartyRepository.save(new ContractPartyEntity(contract, uploader, PartyRole.INITIATOR));

        if (request.getParticipantIds() != null) {
            for (UUID uuid : request.getParticipantIds()) {
                UserEntity participant = userRepository.findByUuid(uuid.toString())
                        .orElseThrow(() -> new CustomException(CustomExceptionEnum.USER_NOT_FOUND));
                if (!participant.getId().equals(uploader.getId())) {
                    contractPartyRepository.save(new ContractPartyEntity(contract, participant, PartyRole.COUNTERPARTY));
                }
            }
        }
        return contract;
    }

    @Transactional
    public ContractEntity updateContract(
            Long contractId,
            ContractUpdateRequestDTO request,
            UserEntity updater,
            MultipartFile file
    ) throws Exception {
        ContractEntity contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.CONTRACT_NOT_FOUND));

        if (contract.getStatus() != ContractStatus.OPEN) {
            throw new CustomException(CustomExceptionEnum.CONTRACT_NOT_MODIFIABLE);
        }

        boolean isParty = contractPartyRepository.findByContractAndParty(contract, updater).isPresent();
        if (!contract.getCreatedBy().getId().equals(updater.getId()) && !isParty) {
            throw new CustomException(CustomExceptionEnum.UNAUTHORIZED);
        }

        ContractVersionEntity previousVersion = contract.getCurrentVersion();
        if (previousVersion != null) {
            previousVersion.setStatus(VersionStatus.ARCHIVED);
            contractVersionRepository.save(previousVersion);
        }

        String newFileHash = generateSHA256FromFile(file.getBytes());
        String newFilePath = s3StorageService.upload(file);
        String bucket = s3StorageService.getBucketName();

        int newVersionNumber = (previousVersion != null) ? previousVersion.getVersionNumber() + 1 : 1;
        ContractVersionEntity newVersion = new ContractVersionEntity(
                contract,
                newVersionNumber,
                newFilePath,
                newFileHash,
                VersionStatus.PENDING_SIGNATURE
        );
        newVersion.setBucketName(bucket);
        newVersion.setStorageProvider("B2");
        contractVersionRepository.save(newVersion);

        contract.setCurrentVersion(newVersion);
        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            contract.setTitle(request.getTitle());
        }
        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            contract.setDescription(request.getDescription());
        }
        contract.setUpdatedAt(LocalDateTime.now());
        contract.setUpdatedBy(updater);
        contractRepository.save(contract);

        return contract;
    }


    private String generateSHA256FromFile(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    @Transactional
    public ContractPartyEntity addParticipantToContract(
            Long contractId,
            AddParticipantRequestDTO request,
            UserEntity actionRequester
    ) {
        ContractEntity contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.CONTRACT_NOT_FOUND));

        if (contract.getStatus() != ContractStatus.OPEN) {
            throw new CustomException(CustomExceptionEnum.CONTRACT_NOT_MODIFIABLE);
        }

        if (!contract.getCreatedBy().getId().equals(actionRequester.getId())) {
            throw new CustomException(CustomExceptionEnum.UNAUTHORIZED);
        }

        UserEntity participantToAdd = userRepository.findByUuid(request.getParticipantUuid().toString())
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.USER_NOT_FOUND));

        if (contractPartyRepository.findByContractAndParty(contract, participantToAdd).isPresent()) {
            throw new CustomException(CustomExceptionEnum.PARTICIPANT_ALREADY_EXISTS);
        }

        if (participantToAdd.getId().equals(contract.getCreatedBy().getId()) && request.getRole() != PartyRole.INITIATOR) {
            throw new CustomException(CustomExceptionEnum.CANNOT_ADD_CREATOR_AS_DIFFERENT_ROLE);
        }

        ContractPartyEntity newContractParty = new ContractPartyEntity(
                contract,
                participantToAdd,
                request.getRole()
        );

        contract.setUpdatedAt(LocalDateTime.now());
        contract.setUpdatedBy(actionRequester);
        contractRepository.save(contract);

        return contractPartyRepository.save(newContractParty);
    }

    @Transactional(readOnly = true)
    public ContractIntegrityVerificationDTO verifyContractIntegrity(Long contractId, int versionNumber, UserEntity requester) {
        ContractEntity contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.CONTRACT_NOT_FOUND));

        boolean isCreator = contract.getCreatedBy().getId().equals(requester.getId());
        boolean isParty = contractPartyRepository.findByContractAndParty(contract, requester).isPresent();
        if (!isCreator && !isParty) {
            throw new CustomException(CustomExceptionEnum.UNAUTHORIZED);
        }

        ContractVersionEntity version = contractVersionRepository.findByContractAndVersionNumber(contract, versionNumber)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.VERSION_NOT_FOUND));

        ContractIntegrityVerificationDTO verificationResult = new ContractIntegrityVerificationDTO(version.getId());

        Optional<BlockchainRecordEntity> blockchainRecordOpt = blockchainRecordRepository.findByContractVersion(version);
        if (blockchainRecordOpt.isEmpty()) {
            verificationResult.getDbVerification().setStatus(ContractIntegrityVerificationDTO.VerificationStatus.DATA_NOT_FOUND);
            verificationResult.getDbVerification().setDetails("해당 계약 버전에 대한 블록체인 기록 정보를 DB에서 찾을 수 없습니다.");
            verificationResult.getBlockchainVerification().setStatus(ContractIntegrityVerificationDTO.VerificationStatus.NOT_CHECKED);
            verificationResult.getBlockchainVerification().setDetails("DB에 블록체인 기록이 없어 비교를 수행할 수 없습니다.");
            verificationResult.setMessage("블록체인 기록이 DB에 존재하지 않아 전체 검증을 진행할 수 없습니다.");
            verificationResult.setOverallSuccess(false);
            return verificationResult;
        }
        BlockchainRecordEntity dbBlockchainRecord = blockchainRecordOpt.get();

        BlockchainMetadataDTO metadataFromChain = null;
        // --- 1단계: DB 기록 무결성 검증 (DB의 metadataHash와 실제 체인 데이터 해시 비교) ---
        try {
            metadataFromChain = blockchainService.getContractMetadataFromBlockchain(version.getId());

            if (metadataFromChain == null) {
                verificationResult.getDbVerification().setStatus(ContractIntegrityVerificationDTO.VerificationStatus.DATA_NOT_FOUND);
                verificationResult.getDbVerification().setDetails("블록체인에서 해당 계약 버전의 메타데이터를 찾을 수 없습니다 (TxID: " + dbBlockchainRecord.getTxHash() + "). DB의 metadataHash 검증 불가.");
                // 블록체인 데이터가 없으면 2단계 비교도 불가
                verificationResult.getBlockchainVerification().setStatus(ContractIntegrityVerificationDTO.VerificationStatus.DATA_NOT_FOUND);
                verificationResult.getBlockchainVerification().setDetails("블록체인에서 메타데이터를 찾을 수 없어 DB와 비교할 수 없습니다.");

            } else {
                String jsonFromChain = objectMapper.writeValueAsString(metadataFromChain);
                String hashOfChainData = generateSHA256ForString(jsonFromChain.getBytes(StandardCharsets.UTF_8));

                if (hashOfChainData.equals(dbBlockchainRecord.getMetadataHash())) {
                    verificationResult.getDbVerification().setStatus(ContractIntegrityVerificationDTO.VerificationStatus.SUCCESS);
                    verificationResult.getDbVerification().setDetails("DB에 기록된 메타데이터 해시가 실제 블록체인 데이터의 해시와 일치합니다.");
                } else {
                    verificationResult.getDbVerification().setStatus(ContractIntegrityVerificationDTO.VerificationStatus.FAILED);
                    verificationResult.getDbVerification().setDetails("DB에 기록된 메타데이터 해시가 실제 블록체인 데이터의 해시와 다릅니다.");
                    verificationResult.getDbVerification().addDiscrepancy("DB 기록된 메타데이터 해시: " + dbBlockchainRecord.getMetadataHash());
                    verificationResult.getDbVerification().addDiscrepancy("블록체인 데이터 재구성 해시: " + hashOfChainData);
                }
            }
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            logger.error("Error during DB record integrity verification (hashing chain data) for versionId {}: {}", version.getId(), e.getMessage());
            verificationResult.getDbVerification().setStatus(ContractIntegrityVerificationDTO.VerificationStatus.ERROR);
            verificationResult.getDbVerification().setDetails("DB 기록 무결성 검증 중 오류 발생 (블록체인 데이터 해싱): " + e.getMessage());
        } catch (Exception e) { // BlockchainService.getContractMetadataFromBlockchain() 예외
            logger.error("Error fetching metadata from blockchain for DB verification, versionId {}: {}", version.getId(), e.getMessage());
            verificationResult.getDbVerification().setStatus(ContractIntegrityVerificationDTO.VerificationStatus.ERROR);
            verificationResult.getDbVerification().setDetails("DB 기록 무결성 검증 중 블록체인 데이터 조회 오류: " + e.getMessage());
            verificationResult.getBlockchainVerification().setStatus(ContractIntegrityVerificationDTO.VerificationStatus.NOT_CHECKED); // 조회 실패 시 2단계도 불가
        }


        // --- 2단계: 블록체인 데이터와 현재 DB 데이터 비교 ---
        // metadataFromChain이 성공적으로 조회된 경우에만 2단계 진행
        if (metadataFromChain != null) {
            try {
                // 현재 DB 상태에서 DTO 재구성
                List<SignatureEntity> currentSignaturesFromDb = signatureRepository.findAllByContractVersion(version);
                List<BlockchainMetadataDTO.SignatureMetadataDTO> currentSignatureDtos = currentSignaturesFromDb.stream()
                        .map(sig -> new BlockchainMetadataDTO.SignatureMetadataDTO(
                                sig.getSigner().getUuid(),
                                sig.getSignatureHash(),
                                sig.getSignedAt()))
                        .collect(Collectors.toList());

                // 재구성 DTO의 fullySignedAt: 블록체인 데이터의 fullySignedAt을 기준으로 비교하거나,
                // DB의 관련 상태(예: contract.updatedAt)와 비교할 수 있음. 여기서는 블록체인 값을 그대로 사용.
                BlockchainMetadataDTO currentDbStateDto = new BlockchainMetadataDTO(
                        version.getId(),
                        version.getFileHash(),
                        contract.getTitle(),
                        contract.getCreatedBy().getUuid(),
                        currentSignatureDtos,
                        metadataFromChain.getFullySignedAt() // 비교를 위해 블록체인 값을 사용
                );

                boolean match = true;
                // ContractVersionId (PK이므로 거의 항상 일치, 확인차원)
                if (!Objects.equals(currentDbStateDto.getContractVersionId(), metadataFromChain.getContractVersionId())) {
                    match = false;
                    verificationResult.getBlockchainVerification().addDiscrepancy("ContractVersionId 불일치: DB=" + currentDbStateDto.getContractVersionId() + ", BC=" + metadataFromChain.getContractVersionId());
                }
                // ContractFileHash
                if (!Objects.equals(currentDbStateDto.getContractFileHash(), metadataFromChain.getContractFileHash())) {
                    match = false;
                    verificationResult.getBlockchainVerification().addDiscrepancy("ContractFileHash 불일치: DB=" + currentDbStateDto.getContractFileHash() + ", BC=" + metadataFromChain.getContractFileHash());
                }
                // ContractTitle
                if (!Objects.equals(currentDbStateDto.getContractTitle(), metadataFromChain.getContractTitle())) {
                    match = false;
                    verificationResult.getBlockchainVerification().addDiscrepancy("ContractTitle 불일치: DB=" + currentDbStateDto.getContractTitle() + ", BC=" + metadataFromChain.getContractTitle());
                }
                // CreatorUuid
                if (!Objects.equals(currentDbStateDto.getCreatorUuid(), metadataFromChain.getCreatorUuid())) {
                    match = false;
                    verificationResult.getBlockchainVerification().addDiscrepancy("CreatorUuid 불일치: DB=" + currentDbStateDto.getCreatorUuid() + ", BC=" + metadataFromChain.getCreatorUuid());
                }
                // FullySignedAt (타임스탬프 비교, 초 단위)
                if (currentDbStateDto.getFullySignedAt() != null && metadataFromChain.getFullySignedAt() != null &&
                        !Objects.equals(currentDbStateDto.getFullySignedAt().withNano(0), metadataFromChain.getFullySignedAt().withNano(0))) {
                    match = false;
                    verificationResult.getBlockchainVerification().addDiscrepancy("FullySignedAt 불일치 (초 단위 비교): DB(BC기준)=" + currentDbStateDto.getFullySignedAt().withNano(0) + ", BC=" + metadataFromChain.getFullySignedAt().withNano(0));
                } else if ((currentDbStateDto.getFullySignedAt() == null && metadataFromChain.getFullySignedAt() != null) ||
                        (currentDbStateDto.getFullySignedAt() != null && metadataFromChain.getFullySignedAt() == null)) {
                    match = false;
                    verificationResult.getBlockchainVerification().addDiscrepancy("FullySignedAt 존재 여부 불일치: DB(BC기준)=" + currentDbStateDto.getFullySignedAt() + ", BC=" + metadataFromChain.getFullySignedAt());
                }


                // Signatures 비교
                List<BlockchainMetadataDTO.SignatureMetadataDTO> sigsFromDbDto = currentDbStateDto.getSignatures();
                List<BlockchainMetadataDTO.SignatureMetadataDTO> sigsFromBcDto = metadataFromChain.getSignatures();

                if (sigsFromDbDto.size() != sigsFromBcDto.size()) {
                    match = false;
                    verificationResult.getBlockchainVerification().addDiscrepancy("서명 개수 불일치: DB=" + sigsFromDbDto.size() + ", BC=" + sigsFromBcDto.size());
                } else {
                    for (BlockchainMetadataDTO.SignatureMetadataDTO sigDb : sigsFromDbDto) {
                        Optional<BlockchainMetadataDTO.SignatureMetadataDTO> sigBcOpt = sigsFromBcDto.stream()
                                .filter(s -> s.getSignerUuid().equals(sigDb.getSignerUuid()))
                                .findFirst();
                        if (sigBcOpt.isEmpty()) {
                            match = false;
                            verificationResult.getBlockchainVerification().addDiscrepancy("서명자 누락 (BC에서 " + sigDb.getSignerUuid() + " 찾을 수 없음)");
                            continue;
                        }
                        BlockchainMetadataDTO.SignatureMetadataDTO sigBc = sigBcOpt.get();
                        if (!Objects.equals(sigDb.getSignatureHash(), sigBc.getSignatureHash())) {
                            match = false;
                            verificationResult.getBlockchainVerification().addDiscrepancy("서명자 " + sigDb.getSignerUuid() + "의 SignatureHash 불일치");
                        }
                        if (sigDb.getSignedAt() != null && sigBc.getSignedAt() != null &&
                                !Objects.equals(sigDb.getSignedAt().withNano(0), sigBc.getSignedAt().withNano(0))) {
                            match = false;
                            verificationResult.getBlockchainVerification().addDiscrepancy("서명자 " + sigDb.getSignerUuid() + "의 SignedAt 불일치 (초 단위 비교)");
                        } else if ((sigDb.getSignedAt() == null && sigBc.getSignedAt() != null) ||
                                (sigDb.getSignedAt() != null && sigBc.getSignedAt() == null)){
                            match = false;
                            verificationResult.getBlockchainVerification().addDiscrepancy("서명자 " + sigDb.getSignerUuid() + "의 SignedAt 존재 여부 불일치");
                        }
                    }
                    // BC에만 있는 서명자 확인
                    for (BlockchainMetadataDTO.SignatureMetadataDTO sigBc : sigsFromBcDto) {
                        if (sigsFromDbDto.stream().noneMatch(s -> s.getSignerUuid().equals(sigBc.getSignerUuid()))) {
                            match = false;
                            verificationResult.getBlockchainVerification().addDiscrepancy("추가 서명자 (DB에서 " + sigBc.getSignerUuid() + " 찾을 수 없음, BC에만 존재)");
                        }
                    }
                }


                if (match) {
                    verificationResult.getBlockchainVerification().setStatus(ContractIntegrityVerificationDTO.VerificationStatus.SUCCESS);
                    verificationResult.getBlockchainVerification().setDetails("현재 DB 데이터와 블록체인에 기록된 메타데이터가 일치합니다.");
                } else {
                    verificationResult.getBlockchainVerification().setStatus(ContractIntegrityVerificationDTO.VerificationStatus.FAILED);
                    verificationResult.getBlockchainVerification().setDetails("현재 DB 데이터와 블록체인 데이터 간 불일치가 발견되었습니다.");
                }

            } catch (Exception e) { // JsonProcessingException, NoSuchAlgorithmException 등
                logger.error("Error during blockchain vs DB data comparison for versionId {}: {}", version.getId(), e.getMessage());
                verificationResult.getBlockchainVerification().setStatus(ContractIntegrityVerificationDTO.VerificationStatus.ERROR);
                verificationResult.getBlockchainVerification().setDetails("블록체인 vs DB 데이터 비교 중 오류 발생: " + e.getMessage());
            }
        } else if (verificationResult.getDbVerification().getStatus() != ContractIntegrityVerificationDTO.VerificationStatus.ERROR &&
                verificationResult.getDbVerification().getStatus() != ContractIntegrityVerificationDTO.VerificationStatus.DATA_NOT_FOUND) {
            // metadataFromChain이 null이지만, dbVerification 단계에서 ERROR나 DATA_NOT_FOUND가 아니었다면 (이런 경우는 거의 없지만 방어적으로)
            verificationResult.getBlockchainVerification().setStatus(ContractIntegrityVerificationDTO.VerificationStatus.NOT_CHECKED);
            verificationResult.getBlockchainVerification().setDetails("블록체인 데이터 조회에 실패하여 DB와 비교할 수 없습니다.");
        }
        // else: metadataFromChain이 null이고, dbVerification 단계에서 이미 DATA_NOT_FOUND 또는 ERROR로 blockchainVerification 상태도 설정되었을 수 있음.


        // 최종 결과 설정
        boolean dbCheckOk = verificationResult.getDbVerification().getStatus() == ContractIntegrityVerificationDTO.VerificationStatus.SUCCESS;
        boolean bcCompareOk = verificationResult.getBlockchainVerification().getStatus() == ContractIntegrityVerificationDTO.VerificationStatus.SUCCESS;

        // 블록체인 기록이 아예 없는 경우는 dbCheckOk, bcCompareOk 모두 false가 될 수 있음 (DATA_NOT_FOUND 상태)
        // 이 경우 overallSuccess는 false.
        if (blockchainRecordOpt.isEmpty() || metadataFromChain == null) { // 블록체인 기록 자체가 없거나 조회가 안된 경우
            verificationResult.setOverallSuccess(false);
            if (verificationResult.getMessage() == null || verificationResult.getMessage().isEmpty()){
                verificationResult.setMessage("계약 무결성 검증 실패: 블록체인에서 데이터를 찾을 수 없습니다.");
            }
        } else if (dbCheckOk && bcCompareOk) {
            verificationResult.setOverallSuccess(true);
            verificationResult.setMessage("계약 무결성 검증 성공: DB와 블록체인 데이터가 일치합니다.");
        } else {
            verificationResult.setOverallSuccess(false);
            if (!dbCheckOk && verificationResult.getDbVerification().getStatus() != ContractIntegrityVerificationDTO.VerificationStatus.DATA_NOT_FOUND) {
                verificationResult.setMessage("계약 무결성 검증 실패: DB 기록 무결성 검증에 실패했습니다.");
            } else if (!bcCompareOk && verificationResult.getBlockchainVerification().getStatus() != ContractIntegrityVerificationDTO.VerificationStatus.DATA_NOT_FOUND && verificationResult.getBlockchainVerification().getStatus() != ContractIntegrityVerificationDTO.VerificationStatus.NOT_CHECKED) {
                verificationResult.setMessage("계약 무결성 검증 실패: 블록체인 데이터와 현재 DB 상태 비교 검증에 실패했습니다.");
            } else if (verificationResult.getMessage() == null || verificationResult.getMessage().isEmpty()){
                verificationResult.setMessage("계약 무결성 검증 실패: 상세 내용을 확인하세요.");
            }
        }
        return verificationResult;
    }

    private String generateSHA256ForString(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    public Page<ContractListDTO> getContractsForUser(String userUuid, Pageable pageable) {
        UserEntity user = userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.USER_NOT_FOUND));

        Page<ContractEntity> contractPage = contractRepository.findContractsByCreatorOrParticipant(user, pageable);

        return contractPage.map(contract -> {
            Integer currentVersionNumber = null;
            if (contract.getCurrentVersion() != null) {
                currentVersionNumber = contract.getCurrentVersion().getVersionNumber();
            }
            // ContractListDTO 생성자를 사용하여 변환
            return new ContractListDTO(
                    contract.getId(),
                    contract.getTitle(),
                    contract.getStatus(),
                    contract.getCreatedAt(),
                    currentVersionNumber
                    // 만약 DTO에 createdByUsername 등이 있다면 여기서 설정
                    // contract.getCreatedBy() != null ? contract.getCreatedBy().getUserName() : null
            );
        });
    }

    @Transactional(readOnly = true)
public ContractDetailDTO getContractDetails(Long contractId, String requesterUuid) {
    try {
        logger.info("계약서 상세 조회 시작 - contractId: {}, requesterUuid: {}", contractId, requesterUuid);
        
        UserEntity requester = userRepository.findByUuid(requesterUuid)
                .orElseThrow(() -> {
                    logger.error("사용자를 찾을 수 없음: {}", requesterUuid);
                    return new CustomException(CustomExceptionEnum.USER_NOT_FOUND);
                });

        ContractEntity contract = contractRepository.findById(contractId)
                .orElseThrow(() -> {
                    logger.error("계약서를 찾을 수 없음: {}", contractId);
                    return new CustomException(CustomExceptionEnum.CONTRACT_NOT_FOUND);
                });

        logger.info("계약서 조회 성공 - 제목: {}, 상태: {}", contract.getTitle(), contract.getStatus());

        // 권한 검사: 요청자가 해당 계약의 생성자이거나 참여자인지 확인
        boolean isCreator = contract.getCreatedBy().getUuid().equals(requesterUuid);
        boolean isParticipant = contractPartyRepository.findByContractAndParty(contract, requester).isPresent();

        if (!isCreator && !isParticipant) {
            logger.error("권한 없음 - contractId: {}, requesterUuid: {}", contractId, requesterUuid);
            throw new CustomException(CustomExceptionEnum.UNAUTHORIZED);
        }

        logger.info("권한 확인 완료 - isCreator: {}, isParticipant: {}", isCreator, isParticipant);

        ContractDetailDTO detailDTO = new ContractDetailDTO();
        detailDTO.setId(contract.getId());
        detailDTO.setTitle(contract.getTitle());
        detailDTO.setDescription(contract.getDescription());
        detailDTO.setStatus(contract.getStatus());
        detailDTO.setCreatedAt(contract.getCreatedAt());
        detailDTO.setUpdatedAt(contract.getUpdatedAt());

        // 생성자 정보 안전하게 설정
        if (contract.getCreatedBy() != null) {
            try {
                detailDTO.setCreatedBy(new UserResponseDTO(
                        contract.getCreatedBy().getId(),
                        contract.getCreatedBy().getUserName(),
                        contract.getCreatedBy().getEmail()
                ));
                logger.debug("생성자 정보 설정 완료");
            } catch (Exception e) {
                logger.error("생성자 정보 설정 중 오류: {}", e.getMessage(), e);
                throw new RuntimeException("생성자 정보 처리 중 오류 발생", e);
            }
        }

        // 수정자 정보 안전하게 설정
        if (contract.getUpdatedBy() != null) {
            try {
                detailDTO.setUpdatedBy(new UserResponseDTO(
                        contract.getUpdatedBy().getId(),
                        contract.getUpdatedBy().getUserName(),
                        contract.getUpdatedBy().getEmail()
                ));
                logger.debug("수정자 정보 설정 완료");
            } catch (Exception e) {
                logger.error("수정자 정보 설정 중 오류: {}", e.getMessage(), e);
                // 수정자 정보는 필수가 아니므로 로그만 남기고 계속 진행
            }
        }

        // 참여자 정보 매핑
        try {
            List<ContractPartyEntity> parties = contractPartyRepository.findByContract(contract);
            logger.debug("참여자 수: {}", parties.size());
            
            List<ParticipantDetailDTO> participantDTOs = parties.stream()
                    .map(party -> {
                        try {
                            return new ParticipantDetailDTO(
                                    party.getParty().getUuid(),
                                    party.getParty().getUserName(),
                                    party.getParty().getEmail(),
                                    party.getRole()
                            );
                        } catch (Exception e) {
                            logger.error("참여자 정보 매핑 중 오류 - partyId: {}, error: {}", 
                                party.getParty().getId(), e.getMessage());
                            throw new RuntimeException("참여자 정보 처리 중 오류 발생", e);
                        }
                    })
                    .collect(Collectors.toList());
            detailDTO.setParticipants(participantDTOs);
            logger.debug("참여자 정보 매핑 완료");
        } catch (Exception e) {
            logger.error("참여자 정보 처리 중 오류: {}", e.getMessage(), e);
            throw new RuntimeException("참여자 정보 처리 중 오류 발생", e);
        }

        // 모든 버전 이력 정보 매핑
        try {
            List<ContractVersionEntity> allVersions = contractVersionRepository.findByContract(contract);
            logger.debug("버전 수: {}", allVersions.size());
            
            // versionNumber를 기준으로 정렬
            allVersions.sort(Comparator.comparingInt(ContractVersionEntity::getVersionNumber));

            List<ContractVersionDetailDTO> versionHistoryDTOs = allVersions.stream()
                    .map(version -> {
                        try {
                            return mapContractVersionToDetailDTO(version);
                        } catch (Exception e) {
                            logger.error("버전 정보 매핑 중 오류 - versionId: {}, error: {}", 
                                version.getId(), e.getMessage());
                            throw new RuntimeException("버전 정보 처리 중 오류 발생", e);
                        }
                    })
                    .collect(Collectors.toList());
            detailDTO.setVersionHistory(versionHistoryDTOs);
            logger.debug("버전 이력 매핑 완료");
        } catch (Exception e) {
            logger.error("버전 이력 처리 중 오류: {}", e.getMessage(), e);
            throw new RuntimeException("버전 이력 처리 중 오류 발생", e);
        }

        // 현재 버전 정보 매핑
        try {
            if (contract.getCurrentVersion() != null) {
                detailDTO.setCurrentVersion(mapContractVersionToDetailDTO(contract.getCurrentVersion()));
                logger.debug("현재 버전 정보 설정 완료");
            } else if (!detailDTO.getVersionHistory().isEmpty()) {
                detailDTO.setCurrentVersion(detailDTO.getVersionHistory().get(detailDTO.getVersionHistory().size() - 1));
                logger.debug("버전 히스토리에서 현재 버전 설정");
            }
        } catch (Exception e) {
            logger.error("현재 버전 정보 처리 중 오류: {}", e.getMessage(), e);
            throw new RuntimeException("현재 버전 정보 처리 중 오류 발생", e);
        }

        logger.info("계약서 상세 조회 완료 - contractId: {}", contractId);
        return detailDTO;
        
    } catch (CustomException e) {
        logger.error("CustomException 발생 - contractId: {}, error: {}", contractId, e.getMessage());
        throw e; // CustomException은 그대로 전파
    } catch (Exception e) {
        logger.error("계약서 상세 조회 중 예상치 못한 오류 - contractId: {}, error: {}", contractId, e.getMessage(), e);
        throw new RuntimeException("계약서 상세 조회 중 오류 발생: " + e.getMessage(), e);
    }
}

    // ContractVersionEntity를 ContractVersionDetailDTO로 변환하는 헬퍼 메소드
    private ContractVersionDetailDTO mapContractVersionToDetailDTO(ContractVersionEntity versionEntity) {
        ContractVersionDetailDTO versionDTO = new ContractVersionDetailDTO();
        versionDTO.setId(versionEntity.getId());
        versionDTO.setVersionNumber(versionEntity.getVersionNumber());
        versionDTO.setFilePath(versionEntity.getFilePath());
        versionDTO.setFileHash(versionEntity.getFileHash());
        versionDTO.setStatus(versionEntity.getStatus());
        versionDTO.setCreatedAt(versionEntity.getCreatedAt());
        versionDTO.setStorageProvider(versionEntity.getStorageProvider());
        versionDTO.setBucketName(versionEntity.getBucketName());

        // 해당 버전에 대한 서명 정보 매핑
        List<SignatureEntity> signatures = signatureRepository.findAllByContractVersion(versionEntity);
        List<SignatureDetailDTO> signatureDTOs = signatures.stream()
                .map(sig -> new SignatureDetailDTO(
                        sig.getSigner().getUuid(),
                        sig.getSigner().getUserName(),
                        sig.getSignedAt(),
                        sig.getSignatureHash() // 실제 서명 값 대신 서명 여부와 시간, 서명자 정보가 더 중요할 수 있음
                ))
                .collect(Collectors.toList());
        versionDTO.setSignatures(signatureDTOs.isEmpty() ? Collections.emptyList() : signatureDTOs); // 빈 리스트 처리

        return versionDTO;
    }


}