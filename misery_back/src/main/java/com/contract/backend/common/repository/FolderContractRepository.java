// FolderContractRepository.java
package com.contract.backend.common.repository;

import com.contract.backend.common.Entity.ContractEntity;
import com.contract.backend.common.Entity.FolderContractEntity;
import com.contract.backend.common.Entity.FolderEntity;
import com.contract.backend.common.Entity.idClass.FolderContractId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FolderContractRepository extends JpaRepository<FolderContractEntity, FolderContractId> {
    
    // 특정 폴더에 있는 계약서들 조회
    @Query("SELECT fc.contract FROM FolderContractEntity fc WHERE fc.folder = :folder AND fc.contract.deletedAt IS NULL")
    List<ContractEntity> findContractsByFolder(@Param("folder") FolderEntity folder);
    
    // 특정 계약서가 어느 폴더에 있는지 조회
    @Query("SELECT fc.folder FROM FolderContractEntity fc WHERE fc.contract = :contract")
    Optional<FolderEntity> findFolderByContract(@Param("contract") ContractEntity contract);
    
    // 특정 폴더-계약서 연결 조회
    Optional<FolderContractEntity> findByFolderAndContract(FolderEntity folder, ContractEntity contract);
    
    // 특정 계약서의 폴더 연결 삭제용
    void deleteByContract(ContractEntity contract);
}