package com.contract.backend.common.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinishLoginRequestDTO {
    private String username;
    private JsonNode credential;  // 클라이언트가 navigator.credentials.get() 으로 받은 credential JSON
}
