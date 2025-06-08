package com.contract.backend.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.contract.backend.common.Entity.ContractEntity;
import com.contract.backend.common.Entity.UserEntity;
import com.contract.backend.common.dto.UserSearchResultDTO;
import com.contract.backend.common.exception.CustomException;
import com.contract.backend.common.exception.CustomExceptionEnum;
import com.contract.backend.common.repository.UserRepository;
import com.contract.backend.common.Entity.FolderEntity;
import com.contract.backend.common.dto.UnifiedSearchResultDTO;
import com.contract.backend.common.repository.ContractRepository;
import com.contract.backend.common.repository.FolderRepository;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final FolderRepository folderRepository;

    public UserService(UserRepository userRepository, ContractRepository contractRepository, FolderRepository folderRepository) {
        this.userRepository = userRepository;
        this.contractRepository = contractRepository;
        this.folderRepository = folderRepository;
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