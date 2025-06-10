package com.contract.backend.common.dto;

import com.contract.backend.common.Entity.enumm.VersionStatus; // VersionStatus enum import
import java.time.LocalDateTime;
import java.util.List;

public class ContractVersionDetailDTO {
    private Long id; // ContractVersionEntity의 ID
    private int versionNumber;
    private String filePath;
    private String fileHash;
    private VersionStatus status;
    private LocalDateTime createdAt;
    private String storageProvider;
    private String bucketName;
    private List<SignatureDetailDTO> signatures; // 해당 버전에 대한 서명 목록

    public ContractVersionDetailDTO() {
    }

    // 생성자, Getters, Setters ...
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public VersionStatus getStatus() {
        return status;
    }

    public void setStatus(VersionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStorageProvider() {
        return storageProvider;
    }

    public void setStorageProvider(String storageProvider) {
        this.storageProvider = storageProvider;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public List<SignatureDetailDTO> getSignatures() {
        return signatures;
    }

    public void setSignatures(List<SignatureDetailDTO> signatures) {
        this.signatures = signatures;
    }
}