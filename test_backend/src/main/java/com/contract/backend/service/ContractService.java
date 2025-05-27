package com.contract.backend.service;

import com.contract.backend.common.Entity.*;
import com.contract.backend.common.Entity.enumm.ContractStatus;
import com.contract.backend.common.Entity.enumm.PartyRole;
import com.contract.backend.common.Entity.enumm.VersionStatus;
import com.contract.backend.common.repository.*;
import com.contract.backend.common.dto.ContractUploadRequestDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
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
                ContractStatus.OPEN
        );
        contract = contractRepository.save(contract);

        // 2. 파일 해시 생성
        String fileHash = generateSHA256(file.getBytes());

        // 3. 파일 업로드
        String filePath = s3StorageService.upload(file);
        String bucket = s3StorageService.getBucketName();

        // 4. ContractVersion 생성
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

        // 5. Contract 업데이트
        contract.setCurrentVersion(version);
        contractRepository.save(contract);

        // 6. 참여자 매핑
        contractPartyRepository.save(new ContractPartyEntity(contract, uploader, PartyRole.INITIATOR));

        for (UUID uuid : request.getParticipantIds()) {
            UserEntity participant = userRepository.findByUuid(uuid)
                    .orElseThrow(() -> new IllegalArgumentException("참여자 UUID를 찾을 수 없습니다: " + uuid));
            contractPartyRepository.save(new ContractPartyEntity(contract, participant, PartyRole.COUNTERPARTY));
        }

        return contract;
    }

    private String generateSHA256(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
