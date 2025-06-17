// FolderRepository.java
package com.contract.backend.common.repository;

import com.contract.backend.common.Entity.FolderEntity;
import com.contract.backend.common.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<FolderEntity, Long> {
    
    // 삭제되지 않은 폴더만 조회
    @Query("SELECT f FROM FolderEntity f WHERE f.createdBy = :user AND f.deletedAt IS NULL")
    List<FolderEntity> findByCreatedByAndNotDeleted(@Param("user") UserEntity user);
    
    // 특정 부모 폴더의 자식 폴더들 조회
    @Query("SELECT f FROM FolderEntity f WHERE f.parent = :parent AND f.deletedAt IS NULL ORDER BY f.name")
    List<FolderEntity> findByParentAndNotDeleted(@Param("parent") FolderEntity parent);
    
    // 루트 폴더들 조회 (부모가 없는 폴더들)
    @Query("SELECT f FROM FolderEntity f WHERE f.parent IS NULL AND f.createdBy = :user AND f.deletedAt IS NULL ORDER BY f.name")
    List<FolderEntity> findRootFoldersByUser(@Param("user") UserEntity user);
    
    // 특정 사용자의 특정 이름 폴더 조회 (중복 체크용)
    @Query("SELECT f FROM FolderEntity f WHERE f.name = :name AND f.parent = :parent AND f.createdBy = :user AND f.deletedAt IS NULL")
    Optional<FolderEntity> findByNameAndParentAndCreatedByAndNotDeleted(
        @Param("name") String name, 
        @Param("parent") FolderEntity parent, 
        @Param("user") UserEntity user
    );
    
    // 루트 레벨에서 같은 이름 폴더 체크
    @Query("SELECT f FROM FolderEntity f WHERE f.name = :name AND f.parent IS NULL AND f.createdBy = :user AND f.deletedAt IS NULL")
    Optional<FolderEntity> findByNameInRootAndCreatedByAndNotDeleted(
        @Param("name") String name, 
        @Param("user") UserEntity user
    );
    
    // ID로 삭제되지 않은 폴더 조회
    @Query("SELECT f FROM FolderEntity f WHERE f.id = :id AND f.deletedAt IS NULL")
    Optional<FolderEntity> findByIdAndNotDeleted(@Param("id") Long id);

    //검색 메도스
    @Query("SELECT f FROM FolderEntity f " +
           "WHERE f.createdBy = :user " +
           "AND LOWER(f.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "AND f.deletedAt IS NULL " +
           "ORDER BY f.name ASC")
    List<FolderEntity> searchUserFoldersByName(
            @Param("user") UserEntity user,
            @Param("query") String query
    );
}