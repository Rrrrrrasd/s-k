package com.contract.backend.common.Entity.idClass;

import java.io.Serializable;
import java.util.Objects;

public class FavoriteContractId implements Serializable {

    // 이 줄을 추가해주세요!
    private static final long serialVersionUID = 1L;

    private Long user;
    private Long contract;

    // ... (기존 생성자, getter/setter, equals/hashCode 메서드) ...
    public FavoriteContractId() {}

    public FavoriteContractId(Long user, Long contract) {
        this.user = user;
        this.contract = contract;
    }

    public Long getUser() { return user; }
    public void setUser(Long user) { this.user = user; }
    public Long getContract() { return contract; }
    public void setContract(Long contract) { this.contract = contract; }

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