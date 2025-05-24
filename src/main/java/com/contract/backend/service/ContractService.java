package com.contract.backend.service;

import com.contract.backend.common.Entity.*;
import com.contract.backend.common.Entity.enumm.ContractStatus;
import com.contract.backend.common.Entity.enumm.PartyRole;
import com.contract.backend.common.Entity.enumm.VersionStatus;
import com.contract.backend.common.dto.AddParticipantRequestDTO;
import com.contract.backend.common.exception.CustomException; // 추가
import com.contract.backend.common.exception.CustomExceptionEnum; // 추가
import com.contract.backend.common.repository.*;
import com.contract.backend.common.dto.ContractUploadRequestDTO;
import com.contract.backend.common.dto.ContractUpdateRequestDTO; // 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.time.LocalDateTime; // 추가 (updatedAt 설정용)
import java.util.List;
import java.util.UUID;

@Service
public class ContractService {

    private final ContractRepository contractRepository;
    private final ContractVersionRepository contractVersionRepository;
    private final ContractPartyRepository contractPartyRepository;
    private final S3StorageService s3StorageService;
    private final UserRepository userRepository;

    public ContractService(
            ContractRepository contractRepository,
            ContractVersionRepository contractVersionRepository,
            ContractPartyRepository contractPartyRepository,
            S3StorageService s3StorageService,
            UserRepository userRepository) {
        this.contractRepository = contractRepository;
        this.contractVersionRepository = contractVersionRepository;
        this.contractPartyRepository = contractPartyRepository;
        this.s3StorageService = s3StorageService;
        this.userRepository = userRepository;
    }

    @Transactional
    public ContractEntity uploadContract(
            ContractUploadRequestDTO request,
            UserEntity uploader,
            MultipartFile file
    ) throws Exception {

        // 1. Contract 생성
        ContractEntity contract = new ContractEntity(
                request.getTitle(),
                request.getDescription(),
                uploader,
                ContractStatus.OPEN // 초기 상태는 OPEN
        );
        contract = contractRepository.save(contract);

        // 2. 파일 해시 생성
        String fileHash = generateSHA256(file.getBytes());

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
                UserEntity participant = userRepository.findByUuid(uuid)
                        .orElseThrow(() -> new IllegalArgumentException("참여자 UUID를 찾을 수 없습니다: " + uuid));
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
    ) throws Exception {
        // 1. 기존 계약 조회
        ContractEntity contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.USER_NOT_FOUND)); // 예외는 적절히 변경 (예: CONTRACT_NOT_FOUND)

        // 2. 계약 상태 및 권한 확인 (예: OPEN 상태이고, 수정 권한이 있는 사용자인지)
        if (contract.getStatus() != ContractStatus.OPEN) {
            throw new CustomException(CustomExceptionEnum.UNAUTHORIZED); // 예외는 적절히 변경 (예: CONTRACT_NOT_MODIFIABLE)
        }

        // (선택적) 수정 권한 확인 로직: contract.getCreatedBy().equals(updater) 또는 참여자인지 등
        // 여기서는 생성자 또는 참여자면 수정 가능하다고 가정 (추후 구체화 필요)
        boolean isParty = contractPartyRepository.findByContractAndParty(contract, updater).isPresent();
        if (!contract.getCreatedBy().getId().equals(updater.getId()) && !isParty) {
            throw new CustomException(CustomExceptionEnum.UNAUTHORIZED); // 수정 권한 없음
        }


        ContractVersionEntity previousVersion = contract.getCurrentVersion();
        if (previousVersion != null) {
            previousVersion.setStatus(VersionStatus.ARCHIVED); // 이전 버전은 ARCHIVED로 변경
            contractVersionRepository.save(previousVersion);
        }

        // 3. 새 파일 해시 생성 및 업로드
        String newFileHash = generateSHA256(file.getBytes());
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

        // 중요: 새 버전이 생성되면 기존 서명은 무효화되고, 새 버전에 대해 다시 서명을 받아야 합니다.
        // 이 로직은 서명 기능 구현 시점에 구체화됩니다. (예: 이전 버전의 SignatureEntity 들을 비활성화)

        return contract;
    }


    private String generateSHA256(byte[] data) throws Exception {
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
        // 1. 계약 조회
        ContractEntity contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.CONTRACT_NOT_FOUND));

        // 2. 계약 상태 확인 (OPEN 상태일 때만 참여자 추가 가능)
        if (contract.getStatus() != ContractStatus.OPEN) {
            throw new CustomException(CustomExceptionEnum.CONTRACT_NOT_MODIFIABLE); // 또는 CANNOT_ADD_PARTICIPANT
        }

        // 3. 참여자 추가 권한 확인 (예: 계약 생성자만 가능하도록)
        if (!contract.getCreatedBy().getId().equals(actionRequester.getId())) {
            throw new CustomException(CustomExceptionEnum.UNAUTHORIZED); // 권한 없음
        }

        // 4. 추가할 사용자 조회
        UserEntity participantToAdd = userRepository.findByUuid(request.getParticipantUuid())
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.USER_NOT_FOUND)); // USER_NOT_FOUND 사용

        // 5. 이미 참여자인지 확인
        if (contractPartyRepository.findByContractAndParty(contract, participantToAdd).isPresent()) {
            throw new CustomException(CustomExceptionEnum.PARTICIPANT_ALREADY_EXISTS); // 혹은 PARTICIPANT_ALREADY_EXISTS
        }

        // 6. 자기 자신(계약 생성자)을 COUNTERPARTY로 다시 추가하는 것 방지 (선택적)
        // 이미 생성자는 INITIATOR로 등록되어 있음
        if (participantToAdd.getId().equals(contract.getCreatedBy().getId()) && request.getRole() != PartyRole.INITIATOR) {
            // 생성자를 다른 역할로 또 추가하려고 하면 막음 (INITIATOR 역할은 이미 있음)
            throw new CustomException(CustomExceptionEnum.CANNOT_ADD_CREATOR_AS_DIFFERENT_ROLE); // 혹은 CANNOT_ADD_CREATOR_AS_DIFFERENT_ROLE
        }


        // 7. 새로운 ContractPartyEntity 생성 및 저장
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
}