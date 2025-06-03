package com.contract.backend.common.dto;

import java.time.LocalDateTime;

public class SignatureDetailDTO {
    private String signerUuid;
    private String signerUsername; // 서명자 이름도 포함하면 좋을 수 있음
    private LocalDateTime signedAt;
    private String signatureHash; // 실제 해시 값보다는 서명 여부와 시간이 중요할 수 있음

    public SignatureDetailDTO() {
    }

    public SignatureDetailDTO(String signerUuid, String signerUsername, LocalDateTime signedAt, String signatureHash) {
        this.signerUuid = signerUuid;
        this.signerUsername = signerUsername;
        this.signedAt = signedAt;
        this.signatureHash = signatureHash;
    }

    // Getters and Setters
    public String getSignerUuid() {
        return signerUuid;
    }

    public void setSignerUuid(String signerUuid) {
        this.signerUuid = signerUuid;
    }

    public String getSignerUsername() {
        return signerUsername;
    }

    public void setSignerUsername(String signerUsername) {
        this.signerUsername = signerUsername;
    }

    public LocalDateTime getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(LocalDateTime signedAt) {
        this.signedAt = signedAt;
    }

    public String getSignatureHash() {
        return signatureHash;
    }

    public void setSignatureHash(String signatureHash) {
        this.signatureHash = signatureHash;
    }
}