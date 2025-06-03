package com.contract.backend.common.repository;

import com.contract.backend.common.Entity.UserEntity;

import io.lettuce.core.dynamic.annotation.Param;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity,Long> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUuid(String uuid);
    default Optional<UserEntity> findByUuid(UUID uuid) {
        return findByUuid(uuid.toString());
    }

     @Query("SELECT u FROM UserEntity u WHERE " +
           "u.uuid != :currentUserUuid AND (" +
           "LOWER(u.userName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.uuid) LIKE LOWER(CONCAT('%', :query, '%'))" +
           ") ORDER BY u.userName")
    List<UserEntity> searchByNameEmailOrUuid(@Param("query") String query, 
                                             @Param("currentUserUuid") String currentUserUuid);
}
