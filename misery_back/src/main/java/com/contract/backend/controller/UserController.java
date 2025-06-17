package com.contract.backend.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.contract.backend.common.dto.UserSearchResultDTO;
import com.contract.backend.common.response.ApiResponse;
import com.contract.backend.service.UserService;

@RestController
@RequestMapping("/api/users") // 새로운 컨트롤러이거나 기존 컨트롤러에 추가
public class UserController {
    
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserSearchResultDTO>>> searchUsers(
            @RequestParam("q") String query,
            @AuthenticationPrincipal String currentUserUuid
    ) {
        try {
            if (query == null || query.trim().length() < 2) {
                return ResponseEntity.ok(ApiResponse.success(Collections.emptyList()));
            }
            
            List<UserSearchResultDTO> searchResults = userService.searchUsers(query.trim(), currentUserUuid);
            return ResponseEntity.ok(ApiResponse.success(searchResults));
        } catch (Exception e) {
            throw new RuntimeException("사용자 검색 실패: " + e.getMessage(), e);
        }
    }
}