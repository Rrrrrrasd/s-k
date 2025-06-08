package com.contract.backend.common.repository;

import com.contract.backend.common.Entity.ContractEntity;
import com.contract.backend.common.Entity.UserEntity;
import com.contract.backend.common.Entity.enumm.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContractRepository extends JpaRepository<ContractEntity, Long> {
    
    // 삭제되지 않은 계약서만 조회
    @Query("SELECT c FROM ContractEntity c WHERE c.createdBy = :user AND c.deletedAt IS NULL")
    List<ContractEntity> findByCreatedByAndNotDeleted(@Param("user") UserEntity user);
    
    @Query("SELECT c FROM ContractEntity c WHERE c.status = :status AND c.deletedAt IS NULL")
    List<ContractEntity> findByStatusAndNotDeleted(@Param("status") ContractStatus status);

    @Query("SELECT DISTINCT c FROM ContractEntity c " +
            "LEFT JOIN ContractPartyEntity cp ON c.id = cp.contract.id " +
            "WHERE (c.createdBy = :user OR cp.party = :user) " +
            "AND c.deletedAt IS NULL")
    Page<ContractEntity> findContractsByCreatorOrParticipant(
            @Param("user") UserEntity user,
            Pageable pageable
    );
    
    // ID로 조회할 때도 삭제되지 않은 것만
    @Query("SELECT c FROM ContractEntity c WHERE c.id = :id AND c.deletedAt IS NULL")
    Optional<ContractEntity> findByIdAndNotDeleted(@Param("id") Long id);

    //검색 메소드
    @Query("SELECT c FROM ContractEntity c " +
           "LEFT JOIN ContractPartyEntity cp ON c.id = cp.contract.id " +
           "WHERE (c.createdBy = :user OR cp.party = :user) " +
           "AND LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "AND c.deletedAt IS NULL " +
           "ORDER BY c.createdAt DESC")
    List<ContractEntity> searchUserContractsByTitle(
           @Param("user") UserEntity user,
           @Param("query") String query
    );
    
    
    List<ContractEntity> findByCreatedBy(UserEntity user);
    List<ContractEntity> findByStatus(ContractStatus status);
}