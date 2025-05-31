package com.contract.backend.common.dto;

import com.contract.backend.common.Entity.enumm.ContractStatus; // ContractStatus enum import
import java.time.LocalDateTime;

public class ContractListDTO {
    private Long id;
    private String title;
    private ContractStatus status;
    private LocalDateTime createdAt;
    private Integer currentVersionNumber;
    // 필요에 따라 계약 생성자 이름 등을 추가할 수 있습니다.
    // private String createdByUsername;

    // 기본 생성자
    public ContractListDTO() {
    }

    // 모든 필드를 포함하는 생성자
    public ContractListDTO(Long id, String title, ContractStatus status, LocalDateTime createdAt, Integer currentVersionNumber) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.createdAt = createdAt;
        this.currentVersionNumber = currentVersionNumber;
    }

    // Getters and Setters
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

    public Integer getCurrentVersionNumber() {
        return currentVersionNumber;
    }

    public void setCurrentVersionNumber(Integer currentVersionNumber) {
        this.currentVersionNumber = currentVersionNumber;
    }

    // public String getCreatedByUsername() {
    //     return createdByUsername;
    // }

    // public void setCreatedByUsername(String createdByUsername) {
    //     this.createdByUsername = createdByUsername;
    // }
}