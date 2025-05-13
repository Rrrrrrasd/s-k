package com.contract.backend.common.dto;

import com.contract.backend.common.Entity.UserEntity;

import java.util.List;
import java.util.UUID;

public class ContractUploadRequestDTO {
    private String title;
    private String description;
    private List<UUID> participantIds; // 또는 UUID List → UserEntity로 변환

    public ContractUploadRequestDTO() {
    }

    public ContractUploadRequestDTO(String title, String description, List<UUID> participantIds) {
        this.title = title;
        this.description = description;
        this.participantIds = participantIds;
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

    public List<UUID> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(List<UUID> participantIds) {
        this.participantIds = participantIds;
    }
}

