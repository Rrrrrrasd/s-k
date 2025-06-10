package com.contract.backend.common.repository;

import com.contract.backend.common.Entity.ContractEntity;
import com.contract.backend.common.Entity.FavoriteContractEntity;
import com.contract.backend.common.Entity.UserEntity;
import com.contract.backend.common.Entity.idClass.FavoriteContractId;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// 복합 키 타입 명시: FavoriteContractEntity, FavoriteContractId
public interface FavoriteContractRepository extends JpaRepository<FavoriteContractEntity, FavoriteContractId> {

    // 특정 사용자가 특정 계약을 즐겨찾기 했는지 확인
    Optional<FavoriteContractEntity> findByUserAndContract(UserEntity user, ContractEntity contract);

    // 특정 사용자의 즐겨찾기 계약 목록 조회 (ContractEntity 반환)
    @Query("SELECT fc.contract FROM FavoriteContractEntity fc WHERE fc.user = :user")
    Page<ContractEntity> findFavoriteContractsByUser(@Param("user") UserEntity user, Pageable pageable);

    // 특정 사용자와 계약 ID로 즐겨찾기 삭제 (복합 키 사용 예시, 서비스 레이어에서 ID 조회 후 객체로 삭제하는 것이 일반적)
    void deleteByUserAndContract(UserEntity user, ContractEntity contract);

	List<FavoriteContractEntity> findAllByUserAndContractIdIn(UserEntity user, List<Long> contractIds);

    // 사용자와 계약 ID로 직접 삭제하는 메서드가 필요하다면 아래와 같이 구현 가능
    // @Modifying // 데이터 변경 쿼리임을 명시
    // @Query("DELETE FROM FavoriteContractEntity fc WHERE fc.user = :user AND fc.contract = :contract")
    // void deleteByUserAndContractManual(@Param("user") UserEntity user, @Param("contract") ContractEntity contract);
}