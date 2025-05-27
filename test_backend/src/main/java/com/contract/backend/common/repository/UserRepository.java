package com.contract.backend.common.repository;

import com.contract.backend.common.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity,Long> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUuid(String uuid);
    default Optional<UserEntity> findByUuid(UUID uuid) {
        return findByUuid(uuid.toString());
    }
}
