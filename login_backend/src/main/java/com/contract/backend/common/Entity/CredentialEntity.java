package com.contract.backend.common.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "credentials")
public class CredentialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 512)
    private String credentialId;

    @Column(nullable = false, length = 512)
    private String userHandle; // UUID 기반

    @Column(nullable = false, length = 2048)
    private String publicKeyCose;

    @Column(nullable = false)
    private long signatureCount;

    @Column(nullable = false)
    private String deviceName;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime lastUsedAt;

    @Column(nullable = false)
    private boolean active = true;

    protected CredentialEntity() {}

    public CredentialEntity(String credentialId, String userHandle, String publicKeyCose, long signatureCount, String deviceName) {
        this.credentialId = credentialId;
        this.userHandle = userHandle;
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
    public String getCredentialId() { return credentialId; }
    public String getUserHandle() { return userHandle; }
    public String getPublicKeyCose() { return publicKeyCose; }
    public long getSignatureCount() { return signatureCount; }
    public String getDeviceName() { return deviceName; }
    public LocalDateTime getLastUsedAt() { return lastUsedAt; }
    public boolean isActive() { return active; }

    //Setter
    public void setSignatureCount(long signatureCount) {this.signatureCount = signatureCount;}
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public void setLastUsedAt(LocalDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }
    public void setActive(boolean active) { this.active = active; }
}
