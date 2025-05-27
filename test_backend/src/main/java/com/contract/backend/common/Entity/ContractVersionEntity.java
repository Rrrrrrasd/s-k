package com.contract.backend.common.Entity;

import com.contract.backend.common.Entity.enumm.VersionStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract_versions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_contract_version_number",
                columnNames = {"contract_id", "version_number"}),
        indexes = @Index(name = "idx_cv_contract", columnList = "contract_id"))
public class ContractVersionEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private ContractEntity contract;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Column(name = "file_path", nullable = false, length = 1024)
    private String filePath;

    @Column(name = "file_hash", nullable = false, length = 64)
    private String fileHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VersionStatus status;

    @Column(name = "storage_provider", length = 20)
    private String storageProvider;

    @Column(name = "bucket_name", length = 255)
    private String bucketName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected ContractVersionEntity() {}

    public ContractVersionEntity(
            ContractEntity contract,
            int versionNumber,
            String filePath,
            String fileHash,
            VersionStatus status
    ) {
        this.contract = contract;
        this.versionNumber = versionNumber;
        this.filePath = filePath;
        this.fileHash = fileHash;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    // getters & setters
    public ContractEntity getContract() {
        return contract;
    }

    public void setContract(ContractEntity contract) {
        this.contract = contract;
    }

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

    public String getStorageProvider() {
        return storageProvider;
    }

    public void setStorageProvider(String storageProvider) {
        this.storageProvider = storageProvider;
    }

    public VersionStatus getStatus() {
        return status;
    }

    public void setStatus(VersionStatus status) {
        this.status = status;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


}
