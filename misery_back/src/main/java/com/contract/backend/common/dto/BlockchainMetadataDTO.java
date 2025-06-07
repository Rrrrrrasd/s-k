package com.contract.backend.common.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"contractVersionId", "contractFileHash", "contractTitle", "creatorUuid", "signatures", "fullySignedAt"})


// 블록체인에 기록될 메타데이터 구조
public class BlockchainMetadataDTO {
    private Long contractVersionId;
    private String contractFileHash;
    private String contractTitle; // 예시 필드, 필요에 따라 추가/제외
    private String creatorUuid;   // 예시 필드
    private List<SignatureMetadataDTO> signatures;
    private LocalDateTime fullySignedAt; // 모든 서명이 완료된 시점 (또는 블록체인 기록 시점)

    // 내부 클래스로 서명 정보 정의
    public static class SignatureMetadataDTO {
        private String signerUuid;
        private String signatureHash;
        private LocalDateTime signedAt;

        public SignatureMetadataDTO(String signerUuid, String signatureHash, LocalDateTime signedAt) {
            this.signerUuid = signerUuid;
            this.signatureHash = signatureHash;
            this.signedAt = signedAt;
        }

        // Getters & Setters
        public String getSignerUuid() { return signerUuid; }
        public void setSignerUuid(String signerUuid) { this.signerUuid = signerUuid; }
        public String getSignatureHash() { return signatureHash; }
        public void setSignatureHash(String signatureHash) { this.signatureHash = signatureHash; }
        public LocalDateTime getSignedAt() { return signedAt; }
        public void setSignedAt(LocalDateTime signedAt) { this.signedAt = signedAt; }
    }

    public BlockchainMetadataDTO(Long contractVersionId, String contractFileHash, String contractTitle, String creatorUuid, List<SignatureMetadataDTO> signatures, LocalDateTime fullySignedAt) {
        this.contractVersionId = contractVersionId;
        this.contractFileHash = contractFileHash;
        this.contractTitle = contractTitle;
        this.creatorUuid = creatorUuid;
        this.signatures = signatures;
        this.fullySignedAt = fullySignedAt;
    }

    // Getters & Setters
    public Long getContractVersionId() { return contractVersionId; }
    public void setContractVersionId(Long contractVersionId) { this.contractVersionId = contractVersionId; }
    public String getContractFileHash() { return contractFileHash; }
    public void setContractFileHash(String contractFileHash) { this.contractFileHash = contractFileHash; }
    public String getContractTitle() { return contractTitle; }
    public void setContractTitle(String contractTitle) { this.contractTitle = contractTitle; }
    public String getCreatorUuid() { return creatorUuid; }
    public void setCreatorUuid(String creatorUuid) { this.creatorUuid = creatorUuid; }
    public List<SignatureMetadataDTO> getSignatures() { return signatures; }
    public void setSignatures(List<SignatureMetadataDTO> signatures) { this.signatures = signatures; }
    public LocalDateTime getFullySignedAt() { return fullySignedAt; }
    public void setFullySignedAt(LocalDateTime fullySignedAt) { this.fullySignedAt = fullySignedAt; }
}