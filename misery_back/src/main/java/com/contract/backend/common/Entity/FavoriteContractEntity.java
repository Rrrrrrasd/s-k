package com.contract.backend.common.Entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

// 복합 키 클래스 정의
class FavoriteContractId implements Serializable {
    private Long user; // UserEntity의 ID 타입
    private Long contract; // ContractEntity의 ID 타입

    // 기본 생성자
    public FavoriteContractId() {}

    // 필드를 받는 생성자
    public FavoriteContractId(Long user, Long contract) {
        this.user = user;
        this.contract = contract;
    }

    // Getter, Setter (JPA 명세에 따라 필요)
    public Long getUser() {
        return user;
    }

    public void setUser(Long user) {
        this.user = user;
    }

    public Long getContract() {
        return contract;
    }

    public void setContract(Long contract) {
        this.contract = contract;
    }

    // equals()와 hashCode() 구현 (복합 키 비교에 필수)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FavoriteContractId that = (FavoriteContractId) o;
        return Objects.equals(user, that.user) && Objects.equals(contract, that.contract);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, contract);
    }
}

@Entity
@Table(name = "favorite_contract")
@IdClass(FavoriteContractId.class) // 복합 키 클래스 지정
public class FavoriteContractEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private UserEntity user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", referencedColumnName = "id", nullable = false)
    private ContractEntity contract;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 기본 생성자 (JPA 명세)
    protected FavoriteContractEntity() {}

    // 생성자
    public FavoriteContractEntity(UserEntity user, ContractEntity contract) {
        this.user = user;
        this.contract = contract;
        this.createdAt = LocalDateTime.now();
    }

    // Getter 메서드
    public UserEntity getUser() {
        return user;
    }

    public ContractEntity getContract() {
        return contract;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setter는 필요에 따라 추가 (JPA는 필드 접근 또는 Getter/Setter를 통해 동작)
    public void setUser(UserEntity user) {
        this.user = user;
    }

    public void setContract(ContractEntity contract) {
        this.contract = contract;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}