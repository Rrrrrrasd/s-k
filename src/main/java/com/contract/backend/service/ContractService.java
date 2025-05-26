package com.contract.backend.service;

import com.contract.backend.common.Entity.*;
import com.contract.backend.common.Entity.enumm.ContractStatus;
import com.contract.backend.common.Entity.enumm.PartyRole;
import com.contract.backend.common.Entity.enumm.VersionStatus;
import com.contract.backend.common.dto.AddParticipantRequestDTO;
import com.contract.backend.common.dto.BlockchainMetadataDTO; // 추가
import com.contract.backend.common.dto.ContractIntegrityVerificationDTO; // 추가
import com.contract.backend.common.exception.CustomException;
import com.contract.backend.common.exception.CustomExceptionEnum;
import com.contract.backend.common.repository.*;
import com.contract.backend.common.dto.ContractUploadRequestDTO;
import com.contract.backend.common.dto.ContractUpdateRequestDTO;
import com.fasterxml.jackson.core.JsonProcessingException; // 추가
import com.fasterxml.jackson.databind.ObjectMapper; // 추가
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // 추가
import org.slf4j.Logger; // 추가
import org.slf4j.LoggerFactory; // 추가
import org.springframework.beans.factory.annotation.Qualifier; // 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets; // 추가
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException; // 추가
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects; // 추가
import java.util.Optional; // 추가
import java.util.UUID;
import java.util.stream.Collectors; // 추가

@Service
public class ContractService {

    private static final Logger logger = LoggerFactory.getLogger(ContractService.class); // 추가

    private final ContractRepository contractRepository;
    private final ContractVersionRepository contractVersionRepository;
    private final ContractPartyRepository contractPartyRepository;
    private final S3StorageService s3StorageService;
    private final UserRepository userRepository;
    private final SignatureRepository signatureRepository; // 추가 (서명 정보 조회용)
    private final BlockchainRecordRepository blockchainRecordRepository; // 추가 (블록체인 기록 조회용)
    private final BlockchainService blockchainService; // 추가 (블록체인 실제 데이터 조회용)
    private final ObjectMapper objectMapper; // 추가 (JSON 처리 및 해시용)


    public ContractService(
            ContractRepository contractRepository,
            ContractVersionRepository contractVersionRepository,
            ContractPartyRepository contractPartyRepository,
            S3StorageService s3StorageService,
            UserRepository userRepository,
            SignatureRepository signatureRepository, // 추가
            BlockchainRecordRepository blockchainRecordRepository, // 추가
            @Qualifier("blockchainService") BlockchainService blockchainService) { // 추가
        this.contractRepository = contractRepository;
        this.contractVersionRepository = contractVersionRepository;
        this.contractPartyRepository = contractPartyRepository;
        this.s3StorageService = s3StorageService;
        this.userRepository = userRepository;
        this.signatureRepository = signatureRepository; // 추가
        this.blockchainRecordRepository = blockchainRecordRepository; // 추가
        this.blockchainService = blockchainService; // 추가
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()); // 추가
    }

    @Transactional
    public ContractEntity uploadContract(
            ContractUploadRequestDTO request,
            UserEntity uploader,
            MultipartFile file
    ) throws Exception { // Exception 대신 구체적인 예외 또는 사용자 정의 예외 사용 고려
        // 1. Contract 생성
        ContractEntity contract = new ContractEntity(
                request.getTitle(),
                request.getDescription(),
                uploader,
                ContractStatus.OPEN // 초기 상태는 OPEN
        );
        contract = contractRepository.save(contract);

        // 2. 파일 해시 생성
        String fileHash = generateSHA256FromFile(file.getBytes());

        // 3. 파일 업로드
        String filePath = s3StorageService.upload(file);
        String bucket = s3StorageService.getBucketName();

        // 4. ContractVersion 생성 (최초 버전은 1)
        ContractVersionEntity version = new ContractVersionEntity(
                contract,
                1, // 최초 버전
                filePath,
                fileHash,
                VersionStatus.PENDING_SIGNATURE // 초기 버전 상태
        );
        version.setBucketName(bucket); // 버킷 이름 설정
        version.setStorageProvider("B2"); // 스토리지 제공자 설정
        contractVersionRepository.save(version);

        // 5. Contract 업데이트 (currentVersion 설정)
        contract.setCurrentVersion(version);
        contractRepository.save(contract); // currentVersion 업데이트 저장

        // 6. 참여자 매핑 (계약 생성자를 INITIATOR로 추가)
        contractPartyRepository.save(new ContractPartyEntity(contract, uploader, PartyRole.INITIATOR));

        // 요청 DTO에 포함된 다른 참여자들을 COUNTERPARTY로 추가
        if (request.getParticipantIds() != null) {
            for (UUID uuid : request.getParticipantIds()) {
                UserEntity participant = userRepository.findByUuid(uuid.toString()) // UUID를 String으로 변환
                        .orElseThrow(() -> new CustomException(CustomExceptionEnum.USER_NOT_FOUND));
                // 자기 자신을 또 추가하지 않도록 체크 (선택 사항)
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
            UserEntity updater, // 수정 요청자
            MultipartFile file
    ) throws Exception { // Exception 대신 구체적인 예외 또는 사용자 정의 예외 사용 고려
        // 1. 기존 계약 조회
        ContractEntity contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.CONTRACT_NOT_FOUND));

        // 2. 계약 상태 및 권한 확인 (예: OPEN 상태이고, 수정 권한이 있는 사용자인지)
        if (contract.getStatus() != ContractStatus.OPEN) {
            throw new CustomException(CustomExceptionEnum.CONTRACT_NOT_MODIFIABLE);
        }

        // (선택적) 수정 권한 확인 로직: contract.getCreatedBy().equals(updater) 또는 참여자인지 등
        boolean isParty = contractPartyRepository.findByContractAndParty(contract, updater).isPresent();
        if (!contract.getCreatedBy().getId().equals(updater.getId()) && !isParty) {
            throw new CustomException(CustomExceptionEnum.UNAUTHORIZED);
        }


        ContractVersionEntity previousVersion = contract.getCurrentVersion();
        if (previousVersion != null) {
            previousVersion.setStatus(VersionStatus.ARCHIVED); // 이전 버전은 ARCHIVED로 변경
            contractVersionRepository.save(previousVersion);
        }

        // 3. 새 파일 해시 생성 및 업로드
        String newFileHash = generateSHA256FromFile(file.getBytes());
        String newFilePath = s3StorageService.upload(file);
        String bucket = s3StorageService.getBucketName();

        // 4. 새 ContractVersion 생성
        int newVersionNumber = (previousVersion != null) ? previousVersion.getVersionNumber() + 1 : 1;
        ContractVersionEntity newVersion = new ContractVersionEntity(
                contract,
                newVersionNumber,
                newFilePath,
                newFileHash,
                VersionStatus.PENDING_SIGNATURE // 새 버전의 초기 상태
        );
        newVersion.setBucketName(bucket);
        newVersion.setStorageProvider("B2");
        contractVersionRepository.save(newVersion);

        // 5. Contract 업데이트
        contract.setCurrentVersion(newVersion);
        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            contract.setTitle(request.getTitle());
        }
        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            contract.setDescription(request.getDescription());
        }
        contract.setUpdatedAt(LocalDateTime.now()); // 수정 시간 기록
        contract.setUpdatedBy(updater); // 수정자 기록
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
            UserEntity actionRequester // 이 작업을 요청한 사용자
    ) {
        ContractEntity contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.CONTRACT_NOT_FOUND));

        // 계약 상태 확인 (OPEN 상태일 때만 참여자 추가 가능)
        if (contract.getStatus() != ContractStatus.OPEN) {
            throw new CustomException(CustomExceptionEnum.CONTRACT_NOT_MODIFIABLE);
        }

        // 참여자 추가 권한 확인 (예: 계약 생성자만 가능하도록)
        if (!contract.getCreatedBy().getId().equals(actionRequester.getId())) {
            // 계약 생성자 외 다른 참여자도 추가 가능하게 하려면 이 로직 수정 또는 역할 기반 권한 확인 필요
            throw new CustomException(CustomExceptionEnum.UNAUTHORIZED);
        }

        UserEntity participantToAdd = userRepository.findByUuid(request.getParticipantUuid().toString()) // UUID를 String으로 변환
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.USER_NOT_FOUND));

        // 이미 참여자인지 확인
        if (contractPartyRepository.findByContractAndParty(contract, participantToAdd).isPresent()) {
            throw new CustomException(CustomExceptionEnum.PARTICIPANT_ALREADY_EXISTS);
        }

        // 자기 자신(계약 생성자)을 COUNTERPARTY로 다시 추가하는 것 방지 (선택적)
        if (participantToAdd.getId().equals(contract.getCreatedBy().getId()) && request.getRole() != PartyRole.INITIATOR) {
            throw new CustomException(CustomExceptionEnum.CANNOT_ADD_CREATOR_AS_DIFFERENT_ROLE);
        }


        ContractPartyEntity newContractParty = new ContractPartyEntity(
                contract,
                participantToAdd,
                request.getRole() // 요청에서 받은 역할 사용
        );

        // 계약 업데이트 시간 기록 (선택 사항)
        contract.setUpdatedAt(LocalDateTime.now());
        contract.setUpdatedBy(actionRequester);
        contractRepository.save(contract);

        return contractPartyRepository.save(newContractParty);
    }

    // --- 무결성 검증 로직 추가 ---
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    public ContractIntegrityVerificationDTO verifyContractIntegrity(Long contractId, int versionNumber, UserEntity requester) {
        ContractEntity contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.CONTRACT_NOT_FOUND));

        // 요청자가 해당 계약의 참여자인지 또는 생성자인지 확인 (권한 검사)
        boolean isCreator = contract.getCreatedBy().getId().equals(requester.getId());
        boolean isParty = contractPartyRepository.findByContractAndParty(contract, requester).isPresent();
        if (!isCreator && !isParty) {
            throw new CustomException(CustomExceptionEnum.UNAUTHORIZED);
        }

        ContractVersionEntity version = contractVersionRepository.findByContractAndVersionNumber(contract, versionNumber)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.VERSION_NOT_FOUND));

        ContractIntegrityVerificationDTO verificationResult = new ContractIntegrityVerificationDTO(version.getId());

        // --- 1단계: DB 내부 데이터 검증 (BlockchainRecord의 metadataHash와 DB 데이터로 재구성한 metadataHash 비교) ---
        Optional<BlockchainRecordEntity> blockchainRecordOpt = blockchainRecordRepository.findByContractVersion(version);
        if (blockchainRecordOpt.isEmpty()) {
            verificationResult.getDbVerification().setStatus(ContractIntegrityVerificationDTO.VerificationStatus.DATA_NOT_FOUND);
            verificationResult.getDbVerification().setDetails("해당 계약 버전에 대한 블록체인 기록 정보를 DB에서 찾을 수 없습니다.");
            verificationResult.setMessage("블록체인 기록이 DB에 존재하지 않아 검증을 진행할 수 없습니다.");
            verificationResult.setOverallSuccess(false);
            return verificationResult;
        }
        BlockchainRecordEntity dbBlockchainRecord = blockchainRecordOpt.get();

        BlockchainMetadataDTO reconstructedDtoFromDb = null; // try 바깥에서도 사용하기 위해 선언 위치 변경

        try {
            // DB에서 서명 정보 가져오기
            List<SignatureEntity> signatures = signatureRepository.findAllByContractVersion(version);
            if (signatures.isEmpty() && version.getStatus() == VersionStatus.SIGNED) {
                verificationResult.getDbVerification().setStatus(ContractIntegrityVerificationDTO.VerificationStatus.FAILED);
                verificationResult.getDbVerification().setDetails("계약 버전은 서명 완료 상태이나, DB에서 서명 정보를 찾을 수 없습니다.");
                verificationResult.getDbVerification().addDiscrepancy("서명 정보 누락");
                // 이 경우에도 이후 블록체인 비교를 시도할 수 있도록 return 하지 않음.
            }

            List<BlockchainMetadataDTO.SignatureMetadataDTO> signatureMetadataDTOs = signatures.stream()
                    .map(sig -> new BlockchainMetadataDTO.SignatureMetadataDTO(
                            sig.getSigner().getUuid(),
                            sig.getSignatureHash(),
                            sig.getSignedAt()))
                    .collect(Collectors.toList());

            reconstructedDtoFromDb = new BlockchainMetadataDTO( // 할당
                    version.getId(),
                    version.getFileHash(),
                    contract.getTitle(),
                    contract.getCreatedBy().getUuid(),
                    signatureMetadataDTOs,
                    dbBlockchainRecord.getRecordedAt()
            );

            String reconstructedJsonFromDb = objectMapper.writeValueAsString(reconstructedDtoFromDb);
            String reconstructedHashFromDb = generateSHA256ForString(reconstructedJsonFromDb.getBytes(StandardCharsets.UTF_8));

            if (reconstructedHashFromDb.equals(dbBlockchainRecord.getMetadataHash())) {
                verificationResult.getDbVerification().setStatus(ContractIntegrityVerificationDTO.VerificationStatus.SUCCESS);
                verificationResult.getDbVerification().setDetails("DB 데이터와 기록된 메타데이터 해시가 일치합니다.");
            } else {
                verificationResult.getDbVerification().setStatus(ContractIntegrityVerificationDTO.VerificationStatus.FAILED);
                verificationResult.getDbVerification().setDetails("DB 데이터로 재구성한 메타데이터 해시가 기록된 해시와 다릅니다.");
                verificationResult.getDbVerification().addDiscrepancy("DB 기록된 메타데이터 해시: " + dbBlockchainRecord.getMetadataHash());
                verificationResult.getDbVerification().addDiscrepancy("DB 데이터 재구성 해시: " + reconstructedHashFromDb);
            }
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            logger.error("Error during DB data verification for versionId {}: {}", version.getId(), e.getMessage());
            verificationResult.getDbVerification().setStatus(ContractIntegrityVerificationDTO.VerificationStatus.ERROR);
            verificationResult.getDbVerification().setDetails("DB 데이터 검증 중 오류 발생: " + e.getMessage());
        }


        // --- 2단계: DB 데이터와 블록체인 데이터 비교 (DB 내부 검증 실패 시에도 정보 제공을 위해 시도 가능) ---
        // reconstructedDtoFromDb가 null이 아닌 경우 (즉, DB 검증 시도 중 심각한 오류가 아니었던 경우) 또는
        // DB 검증 상태가 ERROR가 아닌 경우에만 블록체인 비교를 시도할 수 있음.
        // 혹은, DB 기록이 없을 때(DATA_NOT_FOUND)는 블록체인 비교 자체가 무의미할 수 있음.
        // 여기서는 dbVerification 상태가 ERROR가 아니고, reconstructedDtoFromDb가 생성된 경우에 시도
        if (verificationResult.getDbVerification().getStatus() != ContractIntegrityVerificationDTO.VerificationStatus.ERROR && reconstructedDtoFromDb != null) {
            try {
                BlockchainMetadataDTO metadataFromBlockchain = blockchainService.getContractMetadataFromBlockchain(version.getId());

                if (metadataFromBlockchain == null) {
                    verificationResult.getBlockchainVerification().setStatus(ContractIntegrityVerificationDTO.VerificationStatus.DATA_NOT_FOUND);
                    verificationResult.getBlockchainVerification().setDetails("블록체인에서 해당 계약 버전의 메타데이터를 찾을 수 없습니다 (TxID: " + dbBlockchainRecord.getTxHash() + ").");
                } else {
                    boolean match = true;
                    List<BlockchainMetadataDTO.SignatureMetadataDTO> signatureDTOsFromDb = reconstructedDtoFromDb.getSignatures();
                    List<BlockchainMetadataDTO.SignatureMetadataDTO> signatureDTOsFromBC = metadataFromBlockchain.getSignatures();

                    if (!Objects.equals(reconstructedDtoFromDb.getContractVersionId(), metadataFromBlockchain.getContractVersionId())) {
                        match = false;
                        verificationResult.getBlockchainVerification().addDiscrepancy("ContractVersionId 불일치: DB=" + reconstructedDtoFromDb.getContractVersionId() + ", BC=" + metadataFromBlockchain.getContractVersionId());
                    }
                    if (!Objects.equals(reconstructedDtoFromDb.getContractFileHash(), metadataFromBlockchain.getContractFileHash())) {
                        match = false;
                        verificationResult.getBlockchainVerification().addDiscrepancy("ContractFileHash 불일치: DB=" + reconstructedDtoFromDb.getContractFileHash() + ", BC=" + metadataFromBlockchain.getContractFileHash());
                    }
                    if (!Objects.equals(reconstructedDtoFromDb.getContractTitle(), metadataFromBlockchain.getContractTitle())) {
                        match = false;
                        verificationResult.getBlockchainVerification().addDiscrepancy("ContractTitle 불일치: DB=" + reconstructedDtoFromDb.getContractTitle() + ", BC=" + metadataFromBlockchain.getContractTitle());
                    }
                    if (!Objects.equals(reconstructedDtoFromDb.getCreatorUuid(), metadataFromBlockchain.getCreatorUuid())) {
                        match = false;
                        verificationResult.getBlockchainVerification().addDiscrepancy("CreatorUuid 불일치: DB=" + reconstructedDtoFromDb.getCreatorUuid() + ", BC=" + metadataFromBlockchain.getCreatorUuid());
                    }
                    if (reconstructedDtoFromDb.getFullySignedAt() != null && metadataFromBlockchain.getFullySignedAt() != null &&
                            !Objects.equals(reconstructedDtoFromDb.getFullySignedAt().withNano(0), metadataFromBlockchain.getFullySignedAt().withNano(0))) {
                        match = false;
                        verificationResult.getBlockchainVerification().addDiscrepancy("FullySignedAt 불일치 (초 단위 비교): DB=" + reconstructedDtoFromDb.getFullySignedAt().withNano(0) + ", BC=" + metadataFromBlockchain.getFullySignedAt().withNano(0));
                    } else if ((reconstructedDtoFromDb.getFullySignedAt() == null && metadataFromBlockchain.getFullySignedAt() != null) ||
                            (reconstructedDtoFromDb.getFullySignedAt() != null && metadataFromBlockchain.getFullySignedAt() == null)) {
                        match = false;
                        verificationResult.getBlockchainVerification().addDiscrepancy("FullySignedAt 존재 여부 불일치: DB=" + reconstructedDtoFromDb.getFullySignedAt() + ", BC=" + metadataFromBlockchain.getFullySignedAt());
                    }


                    if (signatureDTOsFromDb.size() != signatureDTOsFromBC.size()) {
                        match = false;
                        verificationResult.getBlockchainVerification().addDiscrepancy("서명 개수 불일치: DB=" + signatureDTOsFromDb.size() + ", BC=" + signatureDTOsFromBC.size());
                    } else {
                        for (BlockchainMetadataDTO.SignatureMetadataDTO sigDb : signatureDTOsFromDb) {
                            Optional<BlockchainMetadataDTO.SignatureMetadataDTO> sigBcOpt = signatureDTOsFromBC.stream()
                                    .filter(s -> s.getSignerUuid().equals(sigDb.getSignerUuid()))
                                    .findFirst();

                            if (sigBcOpt.isEmpty()) {
                                match = false;
                                verificationResult.getBlockchainVerification().addDiscrepancy("서명자 누락 (BC에서 찾을 수 없음): " + sigDb.getSignerUuid());
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
                        for (BlockchainMetadataDTO.SignatureMetadataDTO sigBc : signatureDTOsFromBC) {
                            if (signatureDTOsFromDb.stream().noneMatch(s -> s.getSignerUuid().equals(sigBc.getSignerUuid()))) {
                                match = false;
                                verificationResult.getBlockchainVerification().addDiscrepancy("추가 서명자 (DB에서 찾을 수 없음): " + sigBc.getSignerUuid());
                            }
                        }
                    }

                    if (match) {
                        verificationResult.getBlockchainVerification().setStatus(ContractIntegrityVerificationDTO.VerificationStatus.SUCCESS);
                        verificationResult.getBlockchainVerification().setDetails("DB 데이터와 블록체인에 기록된 메타데이터가 일치합니다.");
                    } else {
                        verificationResult.getBlockchainVerification().setStatus(ContractIntegrityVerificationDTO.VerificationStatus.FAILED);
                        verificationResult.getBlockchainVerification().setDetails("DB 데이터와 블록체인 데이터 간 불일치가 발견되었습니다.");
                    }
                }
            } catch (Exception e) {
                logger.error("Error during blockchain data verification for versionId {}: {}", version.getId(), e.getMessage());
                verificationResult.getBlockchainVerification().setStatus(ContractIntegrityVerificationDTO.VerificationStatus.ERROR);
                verificationResult.getBlockchainVerification().setDetails("블록체인 데이터 검증 중 오류 발생: " + e.getMessage());
            }
        } else if (verificationResult.getDbVerification().getStatus() == ContractIntegrityVerificationDTO.VerificationStatus.ERROR) {
            verificationResult.getBlockchainVerification().setStatus(ContractIntegrityVerificationDTO.VerificationStatus.NOT_CHECKED);
            verificationResult.getBlockchainVerification().setDetails("DB 내부 검증 오류로 인해 블록체인 비교를 수행하지 않았습니다.");
        }


        // 최종 결과 설정
        if (verificationResult.getDbVerification().getStatus() == ContractIntegrityVerificationDTO.VerificationStatus.SUCCESS &&
                verificationResult.getBlockchainVerification().getStatus() == ContractIntegrityVerificationDTO.VerificationStatus.SUCCESS) {
            verificationResult.setOverallSuccess(true);
            verificationResult.setMessage("계약 무결성 검증 성공: DB와 블록체인 데이터가 일치합니다.");
        } else {
            verificationResult.setOverallSuccess(false);
            if (verificationResult.getDbVerification().getStatus() != ContractIntegrityVerificationDTO.VerificationStatus.SUCCESS &&
                    verificationResult.getDbVerification().getStatus() != ContractIntegrityVerificationDTO.VerificationStatus.DATA_NOT_FOUND && // DATA_NOT_FOUND는 이미 메시지 설정됨
                    verificationResult.getDbVerification().getStatus() != ContractIntegrityVerificationDTO.VerificationStatus.ERROR) { // ERROR는 이미 메시지 설정됨
                verificationResult.setMessage("계약 무결성 검증 실패: DB 내부 데이터 검증에 실패했습니다.");
            } else if (verificationResult.getBlockchainVerification().getStatus() != ContractIntegrityVerificationDTO.VerificationStatus.SUCCESS &&
                    verificationResult.getBlockchainVerification().getStatus() != ContractIntegrityVerificationDTO.VerificationStatus.DATA_NOT_FOUND &&
                    verificationResult.getBlockchainVerification().getStatus() != ContractIntegrityVerificationDTO.VerificationStatus.ERROR &&
                    verificationResult.getBlockchainVerification().getStatus() != ContractIntegrityVerificationDTO.VerificationStatus.NOT_CHECKED) {
                verificationResult.setMessage("계약 무결성 검증 실패: 블록체인 데이터 비교 검증에 실패했습니다.");
            } else if (verificationResult.getMessage() == null || verificationResult.getMessage().isEmpty()) { // DATA_NOT_FOUND, ERROR, NOT_CHECKED 등으로 이미 메시지가 설정되지 않은 경우
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
}