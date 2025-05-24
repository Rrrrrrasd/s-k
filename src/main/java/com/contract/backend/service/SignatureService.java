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

@Service
public class SignatureService {

    private final ContractRepository contractRepository;
    private final ContractVersionRepository contractVersionRepository;
    private final SignatureRepository signatureRepository;
    private final ContractPartyRepository contractPartyRepository;
    // UserRepository는 필요시 주입 (AuthService를 통해 UserEntity를 가져오므로 직접 주입은 불필요할 수 있음)

    public SignatureService(ContractRepository contractRepository,
                            ContractVersionRepository contractVersionRepository,
                            SignatureRepository signatureRepository,
                            ContractPartyRepository contractPartyRepository) {
        this.contractRepository = contractRepository;
        this.contractVersionRepository = contractVersionRepository;
        this.signatureRepository = signatureRepository;
        this.contractPartyRepository = contractPartyRepository;
    }

    @Transactional
    public SignatureEntity signContract(Long contractId, UserEntity signer) throws Exception {
        // 1. 계약 및 현재 버전 조회
        ContractEntity contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.CONTRACT_NOT_FOUND));

        ContractVersionEntity currentVersion = contract.getCurrentVersion();
        if (currentVersion == null) {
            // 계약에 currentVersion이 없는 경우는 일반적으로 발생하면 안되지만, 방어적으로 체크
            throw new CustomException(CustomExceptionEnum.VERSION_NOT_FOUND);
        }

        // 2. 계약 상태 및 버전 상태 확인
        if (contract.getStatus() != ContractStatus.OPEN) {
            // 계약이 OPEN 상태가 아니면 서명 불가
            throw new CustomException(CustomExceptionEnum.CANNOT_SIGN_CONTRACT);
        }
        if (currentVersion.getStatus() != VersionStatus.PENDING_SIGNATURE) {
            // 현재 버전이 서명 대기중 상태가 아니면 서명 불가
            throw new CustomException(CustomExceptionEnum.VERSION_NOT_PENDING_SIGNATURE);
        }

        // 3. 서명 권한 확인 (요청자가 계약 참여자인지)
        ContractPartyEntity contractParty = contractPartyRepository.findByContractAndParty(contract, signer)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.UNAUTHORIZED)); // 해당 계약의 참여자가 아님

        // 4. 중복 서명 확인
        Optional<SignatureEntity> existingSignature = signatureRepository.findByContractVersionAndSigner(currentVersion, signer);
        if (existingSignature.isPresent()) {
            // 이미 이 버전에 서명함
            throw new CustomException(CustomExceptionEnum.ALREADY_SIGNED);
        }

        // 5. SignatureEntity 생성 및 저장
        // signatureHash 생성 로직 (예시: 파일 해시 + 서명자 UUID + 시간 -> SHA256)
        // 실제 운영 환경에서는 보다 정교하고 안전한 방법으로 서명 해시(또는 전자서명 값)를 생성/검증해야 합니다.
        String signatureHashValue = generateSimpleSignatureHash(currentVersion.getFileHash(), signer.getUuid());

        SignatureEntity signature = new SignatureEntity(currentVersion, signer, signatureHashValue);
        signatureRepository.save(signature);

        // 6. 모든 필수 참여자 서명 완료 여부 확인
        List<ContractPartyEntity> allPartiesInContract = contractPartyRepository.findByContract(contract);

        // 서명이 필요한 역할을 가진 참여자들만 필터링 (예: INITIATOR, COUNTERPARTY)
        List<UserEntity> requiredSigners = allPartiesInContract.stream()
                .filter(party -> party.getRole() == PartyRole.INITIATOR || party.getRole() == PartyRole.COUNTERPARTY)
                .map(ContractPartyEntity::getParty)
                .collect(Collectors.toList());

        // 현재 버전에 대해 실제로 서명한 사용자 목록 가져오기
        List<SignatureEntity> signaturesForCurrentVersion = signatureRepository.findAllByContractVersion(currentVersion);
        List<UserEntity> actualSigners = signaturesForCurrentVersion.stream()
                .map(SignatureEntity::getSigner)
                .collect(Collectors.toList());

        // 모든 필수 서명자가 서명했는지 확인
        boolean allRequiredHaveSigned = true;
        if (requiredSigners.isEmpty() && !actualSigners.isEmpty()) {
            // 필수 서명자가 없는데 누군가 서명한 경우 (예: 관찰자만 있는 계약은 아니겠지만, 로직상 방어)
            // 혹은, 필수 서명자가 없는 계약은 바로 SIGNED 처리할지 정책 필요. 여기서는 필수 서명자가 있어야 한다고 가정.
            // 만약 필수 서명자가 없는 계약도 유효하다면, 이 로직 수정 필요.
            allRequiredHaveSigned = false; // 혹은 true로 처리하고 바로 SIGNED 상태로 변경
        } else if (requiredSigners.isEmpty()) {
            allRequiredHaveSigned = false; // 필수 서명자가 없으면 아직 서명 완료로 보지 않음 (혹은 정책에 따라 true)
        }


        for (UserEntity requiredSigner : requiredSigners) {
            // 실제 서명자 목록에 필수 서명자가 포함되어 있는지 확인
            if (actualSigners.stream().noneMatch(actual -> actual.getId().equals(requiredSigner.getId()))) {
                allRequiredHaveSigned = false; // 한 명이라도 서명 안 했으면 false
                break;
            }
        }

        if (allRequiredHaveSigned && !requiredSigners.isEmpty()) { // 필수 서명자가 있고, 모두 서명했을 때
            currentVersion.setStatus(VersionStatus.SIGNED); // 버전 상태를 SIGNED로 변경
            contractVersionRepository.save(currentVersion);

            // 계약 전체 상태를 CLOSED로 변경
            contract.setStatus(ContractStatus.CLOSED);
            contract.setUpdatedAt(LocalDateTime.now());
            // 마지막 서명자를 updatedBy로 설정하거나, 시스템 사용자로 설정할 수 있음
            contract.setUpdatedBy(signer);
            contractRepository.save(contract);

            // TODO: (다음 단계) 블록체인에 메타데이터 저장 로직 호출
            // 예: blockchainService.recordContractVersionMetadata(currentVersion);
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