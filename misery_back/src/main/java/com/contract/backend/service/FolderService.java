// FolderService.java
package com.contract.backend.service;

import com.contract.backend.common.Entity.*;
import com.contract.backend.common.dto.*;
import com.contract.backend.common.exception.CustomException;
import com.contract.backend.common.exception.CustomExceptionEnum;
import com.contract.backend.common.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FolderService {

    private static final Logger logger = LoggerFactory.getLogger(FolderService.class);

    private final FolderRepository folderRepository;
    private final FolderContractRepository folderContractRepository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;

    public FolderService(
            FolderRepository folderRepository,
            FolderContractRepository folderContractRepository,
            ContractRepository contractRepository,
            UserRepository userRepository
    ) {
        this.folderRepository = folderRepository;
        this.folderContractRepository = folderContractRepository;
        this.contractRepository = contractRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public FolderResponseDTO createFolder(FolderCreateRequestDTO request, String userUuid) {
        logger.info("폴더 생성 요청 - name: {}, parentId: {}, userUuid: {}", 
                request.getName(), request.getParentId(), userUuid);

        UserEntity user = userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.USER_NOT_FOUND));

        // 폴더명 유효성 검사
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new CustomException(CustomExceptionEnum.CONTRACT_NOT_MODIFIABLE); // 적절한 예외로 변경 필요
        }

        String folderName = request.getName().trim();
        FolderEntity parentFolder = null;
        String folderPath = "/" + folderName;

        // 부모 폴더 확인
        if (request.getParentId() != null) {
            parentFolder = folderRepository.findByIdAndNotDeleted(request.getParentId())
                    .orElseThrow(() -> new CustomException(CustomExceptionEnum.CONTRACT_NOT_FOUND)); // 적절한 예외로 변경 필요

            // 부모 폴더의 소유자 확인
            if (!parentFolder.getCreatedBy().getId().equals(user.getId())) {
                throw new CustomException(CustomExceptionEnum.UNAUTHORIZED);
            }

            folderPath = parentFolder.getPath() + "/" + folderName;
        }

        // 중복 폴더명 검사
        Optional<FolderEntity> existingFolder;
        if (parentFolder != null) {
            existingFolder = folderRepository.findByNameAndParentAndCreatedByAndNotDeleted(
                    folderName, parentFolder, user);
        } else {
            existingFolder = folderRepository.findByNameInRootAndCreatedByAndNotDeleted(
                    folderName, user);
        }

        if (existingFolder.isPresent()) {
            throw new CustomException(CustomExceptionEnum.PARTICIPANT_ALREADY_EXISTS); // 적절한 예외로 변경 필요
        }

        // 폴더 생성
        FolderEntity folder = new FolderEntity(folderName, folderPath, user);
        folder.setParent(parentFolder);
        FolderEntity savedFolder = folderRepository.save(folder);

        logger.info("폴더 생성 완료 - id: {}, name: {}, path: {}", 
                savedFolder.getId(), savedFolder.getName(), savedFolder.getPath());

        return mapToResponseDTO(savedFolder, false);
    }

    @Transactional(readOnly = true)
    public List<FolderResponseDTO> getUserFolders(String userUuid, Long parentId, boolean includeChildren) {
        UserEntity user = userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.USER_NOT_FOUND));

        List<FolderEntity> folders;
        if (parentId == null) {
            // 루트 폴더들 조회
            folders = folderRepository.findRootFoldersByUser(user);
        } else {
            // 특정 부모 폴더의 자식들 조회
            FolderEntity parentFolder = folderRepository.findByIdAndNotDeleted(parentId)
                    .orElseThrow(() -> new CustomException(CustomExceptionEnum.CONTRACT_NOT_FOUND));

            if (!parentFolder.getCreatedBy().getId().equals(user.getId())) {
                throw new CustomException(CustomExceptionEnum.UNAUTHORIZED);
            }

            folders = folderRepository.findByParentAndNotDeleted(parentFolder);
        }

        return folders.stream()
                .map(folder -> mapToResponseDTO(folder, includeChildren))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FolderResponseDTO getFolderDetails(Long folderId, String userUuid, boolean includeContracts) {
        UserEntity user = userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.USER_NOT_FOUND));

        FolderEntity folder = folderRepository.findByIdAndNotDeleted(folderId)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.CONTRACT_NOT_FOUND));

        if (!folder.getCreatedBy().getId().equals(user.getId())) {
            throw new CustomException(CustomExceptionEnum.UNAUTHORIZED);
        }

        FolderResponseDTO response = mapToResponseDTO(folder, true);

        if (includeContracts) {
            List<ContractEntity> contracts = folderContractRepository.findContractsByFolder(folder);
            List<ContractListDTO> contractDTOs = contracts.stream()
                    .map(contract -> new ContractListDTO(
                            contract.getId(),
                            contract.getTitle(),
                            contract.getStatus(),
                            contract.getCreatedAt(),
                            contract.getCurrentVersion() != null ? contract.getCurrentVersion().getVersionNumber() : null
                    ))
                    .collect(Collectors.toList());
            response.setContracts(contractDTOs);
        }

        return response;
    }

    @Transactional
    public FolderResponseDTO updateFolder(Long folderId, FolderUpdateRequestDTO request, String userUuid) {
        logger.info("폴더 수정 요청 - folderId: {}, name: {}, parentId: {}, userUuid: {}", 
                folderId, request.getName(), request.getParentId(), userUuid);

        UserEntity user = userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.USER_NOT_FOUND));

        FolderEntity folder = folderRepository.findByIdAndNotDeleted(folderId)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.CONTRACT_NOT_FOUND));

        if (!folder.getCreatedBy().getId().equals(user.getId())) {
            throw new CustomException(CustomExceptionEnum.UNAUTHORIZED);
        }

        boolean updated = false;

        // 이름 변경
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            String newName = request.getName().trim();
            if (!newName.equals(folder.getName())) {
                // 중복 검사
                Optional<FolderEntity> existingFolder;
                if (folder.getParent() != null) {
                    existingFolder = folderRepository.findByNameAndParentAndCreatedByAndNotDeleted(
                            newName, folder.getParent(), user);
                } else {
                    existingFolder = folderRepository.findByNameInRootAndCreatedByAndNotDeleted(
                            newName, user);
                }

                if (existingFolder.isPresent() && !existingFolder.get().getId().equals(folderId)) {
                    throw new CustomException(CustomExceptionEnum.PARTICIPANT_ALREADY_EXISTS);
                }

                folder.setName(newName);
                updateFolderPath(folder);
                updated = true;
            }
        }

        // 부모 폴더 변경 (폴더 이동)
        if (request.getParentId() != null) {
            if (!Objects.equals(request.getParentId(), 
                    folder.getParent() != null ? folder.getParent().getId() : null)) {
                
                FolderEntity newParent = folderRepository.findByIdAndNotDeleted(request.getParentId())
                        .orElseThrow(() -> new CustomException(CustomExceptionEnum.CONTRACT_NOT_FOUND));

                if (!newParent.getCreatedBy().getId().equals(user.getId())) {
                    throw new CustomException(CustomExceptionEnum.UNAUTHORIZED);
                }

                // 순환 참조 검사
                if (isCircularReference(folder, newParent)) {
                    throw new CustomException(CustomExceptionEnum.CONTRACT_NOT_MODIFIABLE);
                }

                folder.setParent(newParent);
                updateFolderPath(folder);
                updated = true;
            }
        } else if (request.getParentId() == null && folder.getParent() != null) {
            // 루트로 이동
            folder.setParent(null);
            updateFolderPath(folder);
            updated = true;
        }

        if (updated) {
            FolderEntity savedFolder = folderRepository.save(folder);
            logger.info("폴더 수정 완료 - id: {}, name: {}, path: {}", 
                    savedFolder.getId(), savedFolder.getName(), savedFolder.getPath());
        }

        return mapToResponseDTO(folder, false);
    }

    @Transactional
    public void deleteFolder(Long folderId, String userUuid) {
        logger.info("폴더 삭제 요청 - folderId: {}, userUuid: {}", folderId, userUuid);

        UserEntity user = userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.USER_NOT_FOUND));

        FolderEntity folder = folderRepository.findByIdAndNotDeleted(folderId)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.CONTRACT_NOT_FOUND));

        if (!folder.getCreatedBy().getId().equals(user.getId())) {
            throw new CustomException(CustomExceptionEnum.UNAUTHORIZED);
        }

        // 하위 폴더가 있는지 확인
        List<FolderEntity> children = folderRepository.findByParentAndNotDeleted(folder);
        if (!children.isEmpty()) {
            throw new CustomException(CustomExceptionEnum.CONTRACT_NOT_MODIFIABLE);
        }

        // 폴더 내 계약서들을 루트로 이동
        List<ContractEntity> contracts = folderContractRepository.findContractsByFolder(folder);
        for (ContractEntity contract : contracts) {
            folderContractRepository.deleteByContract(contract);
        }

        // 논리적 삭제
        folder.setDeletedAt(LocalDateTime.now());
        folderRepository.save(folder);

        logger.info("폴더 삭제 완료 - folderId: {}", folderId);
    }

    @Transactional
    public void moveContractToFolder(ContractMoveToFolderRequestDTO request, String userUuid) {
        UserEntity user = userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.USER_NOT_FOUND));

        ContractEntity contract = contractRepository.findByIdAndNotDeleted(request.getContractId())
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.CONTRACT_NOT_FOUND));

        // 계약서 권한 확인
        if (!contract.getCreatedBy().getId().equals(user.getId())) {
            throw new CustomException(CustomExceptionEnum.UNAUTHORIZED);
        }

        // 기존 폴더 연결 제거
        folderContractRepository.deleteByContract(contract);

        // 새 폴더로 이동
        if (request.getFolderId() != null) {
            FolderEntity targetFolder = folderRepository.findByIdAndNotDeleted(request.getFolderId())
                    .orElseThrow(() -> new CustomException(CustomExceptionEnum.CONTRACT_NOT_FOUND));

            if (!targetFolder.getCreatedBy().getId().equals(user.getId())) {
                throw new CustomException(CustomExceptionEnum.UNAUTHORIZED);
            }

            FolderContractEntity folderContract = new FolderContractEntity(targetFolder, contract);
            folderContractRepository.save(folderContract);
        }
        // folderId가 null이면 루트로 이동 (연결 제거만)
    }

    private void updateFolderPath(FolderEntity folder) {
        String newPath;
        if (folder.getParent() != null) {
            newPath = folder.getParent().getPath() + "/" + folder.getName();
        } else {
            newPath = "/" + folder.getName();
        }
        folder.setPath(newPath);

        // 하위 폴더들의 경로도 업데이트
        List<FolderEntity> children = folderRepository.findByParentAndNotDeleted(folder);
        for (FolderEntity child : children) {
            updateFolderPath(child);
            folderRepository.save(child);
        }
    }

    private boolean isCircularReference(FolderEntity folder, FolderEntity potentialParent) {
        FolderEntity current = potentialParent;
        while (current != null) {
            if (current.getId().equals(folder.getId())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    private FolderResponseDTO mapToResponseDTO(FolderEntity folder, boolean includeChildren) {
        FolderResponseDTO dto = new FolderResponseDTO(
                folder.getId(),
                folder.getName(),
                folder.getPath(),
                folder.getParent() != null ? folder.getParent().getId() : null,
                folder.getCreatedAt(),
                new UserResponseDTO(
                        folder.getCreatedBy().getId(),
                        folder.getCreatedBy().getUserName(),
                        folder.getCreatedBy().getEmail()
                )
        );

        if (folder.getParent() != null) {
            dto.setParentName(folder.getParent().getName());
        }

        if (includeChildren) {
            List<FolderEntity> children = folderRepository.findByParentAndNotDeleted(folder);
            List<FolderResponseDTO> childrenDTOs = children.stream()
                    .map(child -> mapToResponseDTO(child, false))
                    .collect(Collectors.toList());
            dto.setChildren(childrenDTOs);
            dto.setChildrenCount(children.size());
        }

        // 계약서 개수 설정
        List<ContractEntity> contracts = folderContractRepository.findContractsByFolder(folder);
        dto.setContractsCount(contracts.size());

        return dto;
    }
}