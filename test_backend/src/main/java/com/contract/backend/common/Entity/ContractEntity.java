package com.contract.backend.common.Entity;

import com.contract.backend.common.Entity.enumm.ContractStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contracts",
        indexes = {
                @Index(name = "idx_contract_created_by", columnList = "created_by"),
                @Index(name = "idx_contract_status", columnList = "status")
        })
public class ContractEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private UserEntity createdBy;

    // 현재 활성 버전 (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_version")
    private ContractVersionEntity currentVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private UserEntity updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected ContractEntity() {}

    public ContractEntity(String title, String description, UserEntity createdBy, ContractStatus status) {
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    //getter & setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UserEntity getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserEntity createdBy) {
        this.createdBy = createdBy;
    }

    public ContractVersionEntity getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(ContractVersionEntity currentVersion) {
        this.currentVersion = currentVersion;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public void setStatus(ContractStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UserEntity getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UserEntity updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
