package com.contract.backend.common.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blockchain_records",
        indexes = {
                @Index(name = "idx_br_cv", columnList = "contract_version_id"),
                @Index(name = "idx_br_recorded_at", columnList = "recorded_at")
        })
public class BlockchainRecordEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_version_id", nullable = false)
    private ContractVersionEntity contractVersion;

    @Column(name = "metadata_hash", nullable = false, length = 64)
    private String metadataHash;

    @Column(name = "tx_hash", nullable = false, length = 128)
    private String txHash;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    protected BlockchainRecordEntity() {}

    public BlockchainRecordEntity(
            ContractVersionEntity contractVersion,
            String metadataHash,
            String txHash
    ) {
        this.contractVersion = contractVersion;
        this.metadataHash    = metadataHash;
        this.txHash          = txHash;
        this.recordedAt      = LocalDateTime.now();
    }

    // getters & setters …

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ContractVersionEntity getContractVersion() {
        return contractVersion;
    }

    public void setContractVersion(ContractVersionEntity contractVersion) {
        this.contractVersion = contractVersion;
    }

    public String getMetadataHash() {
        return metadataHash;
    }

    public void setMetadataHash(String metadataHash) {
        this.metadataHash = metadataHash;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }
}
