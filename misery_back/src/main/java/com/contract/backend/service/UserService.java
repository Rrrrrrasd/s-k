package com.contract.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.contract.backend.common.Entity.UserEntity;
import com.contract.backend.common.dto.UserSearchResultDTO;
import com.contract.backend.common.exception.CustomException;
import com.contract.backend.common.exception.CustomExceptionEnum;
import com.contract.backend.common.repository.UserRepository;

@Service
public class UserService {
    
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserSearchResultDTO> searchUsers(String query, String currentUserUuid) {
        List<UserEntity> users = userRepository.searchByNameEmailOrUuid(query, currentUserUuid);
        
        return users.stream()
                .map(user -> new UserSearchResultDTO(
                        user.getId(),
                        user.getUuid(),
                        user.getUserName(),
                        user.getEmail()
                ))
                .limit(10)
                .collect(Collectors.toList());
    }

    // AuthService에서 사용하는 메서드들을 이곳으로 이동할 수도 있습니다
    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.EMAIL_NOT_FOUND));
    }

    public UserEntity findByUuid(String uuid) {
        return userRepository.findByUuid(uuid)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.USER_NOT_FOUND));
    }
}