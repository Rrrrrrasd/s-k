package com.contract.backend.common.Entity;

import jakarta.persistence.*;
import org.apache.catalina.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "credentials",
        indexes = @Index(name = "idx_credentials_user", columnList = "user_id"))
public class CredentialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //추가
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "credential_id",nullable = false, unique = true, length = 512)
    private String credentialId;

    @Lob
    @Column(name = "public_key_cose",nullable = false, columnDefinition = "TEXT")
    private String publicKeyCose;

    @Column(name = "signature_count" ,nullable = false)
    private long signatureCount;

    @Column(name = "device_name",nullable = false)
    private String deviceName;

    @Column(name = "created_at",nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(nullable = false)
    private boolean active = true;

    protected CredentialEntity() {}

    public CredentialEntity(UserEntity user,
                            String credentialId,
                            String publicKeyCose,
                            long signatureCount,
                            String deviceName) {
        this.user = user;
        this.credentialId = credentialId;
        this.publicKeyCose = publicKeyCose;
        this.signatureCount = signatureCount;
        this.deviceName = deviceName;
        this.active = true;
        this.lastUsedAt = null;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    //Getter
    public Long getId() { return id; }
    public UserEntity getUser() { return user; }
    public String getCredentialId() { return credentialId; }
    public String getPublicKeyCose() { return publicKeyCose; }
    public long getSignatureCount() { return signatureCount; }
    public String getDeviceName() { return deviceName; }
    public LocalDateTime getLastUsedAt() { return lastUsedAt; }
    public boolean isActive() { return active; }

    //Setter
    public void setUser(UserEntity user) { this.user = user; }
    public void setSignatureCount(long signatureCount) {this.signatureCount = signatureCount;}
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public void setLastUsedAt(LocalDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }
    public void setActive(boolean active) { this.active = active; }
}
