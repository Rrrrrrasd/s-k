package com.contract.backend.common.repository;

import com.contract.backend.common.Entity.ContractEntity;
import com.contract.backend.common.Entity.ContractVersionEntity;
import com.contract.backend.common.Entity.enumm.VersionStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContractVersionRepository extends JpaRepository<ContractVersionEntity, Long> {
    List<ContractVersionEntity> findByContract(ContractEntity contract);
    Optional<ContractVersionEntity> findByContractAndVersionNumber(ContractEntity contract, int versionNumber);
    List<ContractVersionEntity> findByStatus(VersionStatus status);
    
    /**
     * 파일 경로로 계약서 버전 찾기 (미리보기/다운로드용)
     */
    @Query("SELECT cv FROM ContractVersionEntity cv WHERE cv.filePath = :filePath")
    Optional<ContractVersionEntity> findByFilePath(@Param("filePath") String filePath);
    
    /**
     * 파일 경로로 계약서 버전 찾기 (삭제되지 않은 계약서만)
     */
    @Query("SELECT cv FROM ContractVersionEntity cv " +
           "WHERE cv.filePath = :filePath " +
           "AND cv.contract.deletedAt IS NULL")
    Optional<ContractVersionEntity> findByFilePathAndContractNotDeleted(@Param("filePath") String filePath);
}
