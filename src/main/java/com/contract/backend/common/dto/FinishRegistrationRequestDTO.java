package com.contract.backend.common.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinishRegistrationRequestDTO {
    private String username;
    private JsonNode credential;
}
