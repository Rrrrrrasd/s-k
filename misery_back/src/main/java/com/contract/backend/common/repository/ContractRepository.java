package com.contract.backend.common.repository;

import com.contract.backend.common.Entity.ContractEntity;
import com.contract.backend.common.Entity.UserEntity;
import com.contract.backend.common.Entity.enumm.ContractStatus;
import org.springframework.data.domain.Page; // Page import 추가
import org.springframework.data.domain.Pageable; // Pageable import 추가
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Query import 추가
import org.springframework.data.repository.query.Param; // Param import 추가

import java.util.List;

public interface ContractRepository extends JpaRepository<ContractEntity, Long> {
    List<ContractEntity> findByCreatedBy(UserEntity user);
    List<ContractEntity> findByStatus(ContractStatus status);

    @Query("SELECT DISTINCT c FROM ContractEntity c " +
            "LEFT JOIN ContractPartyEntity cp ON c.id = cp.contract.id " +
            "WHERE c.createdBy = :user OR cp.party = :user") // ORDER BY 절 삭제
    Page<ContractEntity> findContractsByCreatorOrParticipant(
            @Param("user") UserEntity user,
            Pageable pageable
    );
}
