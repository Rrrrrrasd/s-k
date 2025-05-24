package com.contract.backend.common.dto;

import com.contract.backend.common.Entity.enumm.PartyRole; // PartyRole enum import
import java.util.UUID;

public class AddParticipantRequestDTO {
    private UUID participantUuid;
    private PartyRole role; // 참여자의 역할을 명시적으로 받음

    public AddParticipantRequestDTO() {
    }

    public AddParticipantRequestDTO(UUID participantUuid, PartyRole role) {
        this.participantUuid = participantUuid;
        this.role = role;
    }

    public UUID getParticipantUuid() {
        return participantUuid;
    }

    public void setParticipantUuid(UUID participantUuid) {
        this.participantUuid = participantUuid;
    }

    public PartyRole getRole() {
        return role;
    }

    public void setRole(PartyRole role) {
        this.role = role;
    }
}