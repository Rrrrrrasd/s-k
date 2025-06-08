// FolderController.java
package com.contract.backend.controller;

import com.contract.backend.common.dto.*;
import com.contract.backend.common.response.ApiResponse;
import com.contract.backend.service.FolderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

    private static final Logger logger = LoggerFactory.getLogger(FolderController.class);
    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    /**
     * 폴더 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FolderResponseDTO>> createFolder(
            @RequestBody FolderCreateRequestDTO request,
            @AuthenticationPrincipal String userUuid
    ) {
        try {
            logger.info("폴더 생성 API 호출 - name: {}, parentId: {}, userUuid: {}", 
                    request.getName(), request.getParentId(), userUuid);
            
            FolderResponseDTO folder = folderService.createFolder(request, userUuid);
            return ResponseEntity.ok(ApiResponse.success(folder));
        } catch (Exception e) {
            logger.error("폴더 생성 실패 - userUuid: {}, error: {}", userUuid, e.getMessage(), e);
            throw new RuntimeException("폴더 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 사용자의 폴더 목록 조회
     * @param parentId null이면 루트 폴더들, 값이 있으면 해당 폴더의 하위 폴더들
     * @param includeChildren 하위 폴더 정보 포함 여부
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<FolderResponseDTO>>> getUserFolders(
            @RequestParam(required = false) Long parentId,
            @RequestParam(defaultValue = "false") boolean includeChildren,
            @AuthenticationPrincipal String userUuid
    ) {
        try {
            logger.debug("폴더 목록 조회 API 호출 - parentId: {}, includeChildren: {}, userUuid: {}", 
                    parentId, includeChildren, userUuid);
            
            List<FolderResponseDTO> folders = folderService.getUserFolders(userUuid, parentId, includeChildren);
            return ResponseEntity.ok(ApiResponse.success(folders));
        } catch (Exception e) {
            logger.error("폴더 목록 조회 실패 - userUuid: {}, error: {}", userUuid, e.getMessage(), e);
            throw new RuntimeException("폴더 목록 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 특정 폴더 상세 조회
     */
    @GetMapping("/{folderId}")
    public ResponseEntity<ApiResponse<FolderResponseDTO>> getFolderDetails(
            @PathVariable Long folderId,
            @RequestParam(defaultValue = "true") boolean includeContracts,
            @AuthenticationPrincipal String userUuid
    ) {
        try {
            logger.debug("폴더 상세 조회 API 호출 - folderId: {}, includeContracts: {}, userUuid: {}", 
                    folderId, includeContracts, userUuid);
            
            FolderResponseDTO folder = folderService.getFolderDetails(folderId, userUuid, includeContracts);
            return ResponseEntity.ok(ApiResponse.success(folder));
        } catch (Exception e) {
            logger.error("폴더 상세 조회 실패 - folderId: {}, userUuid: {}, error: {}", folderId, userUuid, e.getMessage(), e);
            throw new RuntimeException("폴더 상세 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 폴더 수정 (이름 변경, 이동)
     */
    @PutMapping("/{folderId}")
    public ResponseEntity<ApiResponse<FolderResponseDTO>> updateFolder(
            @PathVariable Long folderId,
            @RequestBody FolderUpdateRequestDTO request,
            @AuthenticationPrincipal String userUuid
    ) {
        try {
            logger.info("폴더 수정 API 호출 - folderId: {}, name: {}, parentId: {}, userUuid: {}", 
                    folderId, request.getName(), request.getParentId(), userUuid);
            
            FolderResponseDTO folder = folderService.updateFolder(folderId, request, userUuid);
            return ResponseEntity.ok(ApiResponse.success(folder));
        } catch (Exception e) {
            logger.error("폴더 수정 실패 - folderId: {}, userUuid: {}, error: {}", folderId, userUuid, e.getMessage(), e);
            throw new RuntimeException("폴더 수정에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 폴더 삭제
     */
    @DeleteMapping("/{folderId}")
    public ResponseEntity<ApiResponse<Void>> deleteFolder(
            @PathVariable Long folderId,
            @AuthenticationPrincipal String userUuid
    ) {
        try {
            logger.info("폴더 삭제 API 호출 - folderId: {}, userUuid: {}", folderId, userUuid);
            
            folderService.deleteFolder(folderId, userUuid);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            logger.error("폴더 삭제 실패 - folderId: {}, userUuid: {}, error: {}", folderId, userUuid, e.getMessage(), e);
            throw new RuntimeException("폴더 삭제에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 계약서를 폴더로 이동
     */
    @PostMapping("/move-contract")
    public ResponseEntity<ApiResponse<Void>> moveContractToFolder(
            @RequestBody ContractMoveToFolderRequestDTO request,
            @AuthenticationPrincipal String userUuid
    ) {
        try {
            logger.info("계약서 폴더 이동 API 호출 - contractId: {}, folderId: {}, userUuid: {}", 
                    request.getContractId(), request.getFolderId(), userUuid);
            
            folderService.moveContractToFolder(request, userUuid);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            logger.error("계약서 폴더 이동 실패 - contractId: {}, folderId: {}, userUuid: {}, error: {}", 
                    request.getContractId(), request.getFolderId(), userUuid, e.getMessage(), e);
            throw new RuntimeException("계약서 폴더 이동에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 폴더 트리 구조 조회 (전체 계층 구조)
     */
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<FolderResponseDTO>>> getFolderTree(
            @AuthenticationPrincipal String userUuid
    ) {
        try {
            logger.debug("폴더 트리 조회 API 호출 - userUuid: {}", userUuid);
            
            // 루트 폴더들을 조회하되 하위 폴더들도 포함
            List<FolderResponseDTO> folders = folderService.getUserFolders(userUuid, null, true);
            return ResponseEntity.ok(ApiResponse.success(folders));
        } catch (Exception e) {
            logger.error("폴더 트리 조회 실패 - userUuid: {}, error: {}", userUuid, e.getMessage(), e);
            throw new RuntimeException("폴더 트리 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 특정 폴더의 계약서 목록 조회
     */
    @GetMapping("/{folderId}/contracts")
    public ResponseEntity<ApiResponse<List<ContractListDTO>>> getFolderContracts(
            @PathVariable Long folderId,
            @AuthenticationPrincipal String userUuid
    ) {
        try {
            logger.debug("폴더 내 계약서 목록 조회 API 호출 - folderId: {}, userUuid: {}", folderId, userUuid);
            
            FolderResponseDTO folder = folderService.getFolderDetails(folderId, userUuid, true);
            return ResponseEntity.ok(ApiResponse.success(folder.getContracts()));
        } catch (Exception e) {
            logger.error("폴더 내 계약서 목록 조회 실패 - folderId: {}, userUuid: {}, error: {}", 
                    folderId, userUuid, e.getMessage(), e);
            throw new RuntimeException("폴더 내 계약서 목록 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }
}