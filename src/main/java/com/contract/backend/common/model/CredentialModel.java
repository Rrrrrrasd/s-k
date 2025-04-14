package com.contract.backend.common.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CredentialModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // WebAuthn에서 제공하는 credential ID (Base64로 인코딩된 값 저장)
    @Column(nullable = false, unique = true, length = 512)
    private String credentialId;

    // 공개키(Base64 인코딩)
    @Column(nullable = false, length = 2000)
    private String publicKeyCose;

    // 등록 시 마지막 인증 시점의 counter (replay 공격 방지)
    @Column(nullable = false)
    private long signatureCount;

    // 사용자와의 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;

    @Column(updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }
}
