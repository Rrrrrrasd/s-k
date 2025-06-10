package com.contract.backend.common.dto;

import com.contract.backend.common.Entity.enumm.PartyRole; // PartyRole enum import

public class ParticipantDetailDTO {
    private String userUuid;
    private String username;
    private String email;
    private PartyRole role;
    // 필요시 추가 정보 (예: 초대 일시)

    public ParticipantDetailDTO() {
    }

    public ParticipantDetailDTO(String userUuid, String username, String email, PartyRole role) {
        this.userUuid = userUuid;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    // Getters and Setters
    public String getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public PartyRole getRole() {
        return role;
    }

    public void setRole(PartyRole role) {
        this.role = role;
    }
}