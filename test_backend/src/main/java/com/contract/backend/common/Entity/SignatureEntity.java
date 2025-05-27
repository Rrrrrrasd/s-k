package com.contract.backend.common.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "signatures",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_sig_cv_signer",
                columnNames = {"contract_version_id", "signer_id"}),
        indexes = {
                @Index(name = "idx_sig_cv", columnList = "contract_version_id")
        })
public class SignatureEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_version_id", nullable = false)
    private ContractVersionEntity contractVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "signer_id", nullable = false)
    private UserEntity signer;

    @Column(name = "signed_at", nullable = false)
    private LocalDateTime signedAt;

    @Column(name = "signature_hash", nullable = false, length = 64)
    private String signatureHash;

    protected SignatureEntity() {}

    public SignatureEntity(
            ContractVersionEntity contractVersion,
            UserEntity signer,
            String signatureHash
    ) {
        this.contractVersion = contractVersion;
        this.signer          = signer;
        this.signatureHash   = signatureHash;
        this.signedAt        = LocalDateTime.now();
    }

    // getters & setters

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

    public UserEntity getSigner() {
        return signer;
    }

    public void setSigner(UserEntity signer) {
        this.signer = signer;
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
