package com.contract.backend.common.dto;

import com.contract.backend.common.Entity.enumm.ContractStatus; // ContractStatus enum import
import java.time.LocalDateTime;
import java.util.List;

public class ContractDetailDTO {
    private Long id;
    private String title;
    private String description;
    private UserResponseDTO createdBy; // UserResponseDTO 재활용 또는 새로운 CreatorDTO
    private ContractStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserResponseDTO updatedBy; // UserResponseDTO 재활용 또는 새로운 EditorDTO

    private ContractVersionDetailDTO currentVersion; // 현재 활성 버전의 상세 정보
    private List<ContractVersionDetailDTO> versionHistory; // 모든 버전 이력 (선택 사항, 필요시 포함)
    private List<ParticipantDetailDTO> participants; // 계약 참여자 목록

    // 기본 생성자, 모든 필드 생성자, Getters, Setters ...

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UserResponseDTO getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserResponseDTO createdBy) {
        this.createdBy = createdBy;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public void setStatus(ContractStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UserResponseDTO getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UserResponseDTO updatedBy) {
        this.updatedBy = updatedBy;
    }

    public ContractVersionDetailDTO getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(ContractVersionDetailDTO currentVersion) {
        this.currentVersion = currentVersion;
    }

    public List<ContractVersionDetailDTO> getVersionHistory() {
        return versionHistory;
    }

    public void setVersionHistory(List<ContractVersionDetailDTO> versionHistory) {
        this.versionHistory = versionHistory;
    }

    public List<ParticipantDetailDTO> getParticipants() {
        return participants;
    }

    public void setParticipants(List<ParticipantDetailDTO> participants) {
        this.participants = participants;
    }
}