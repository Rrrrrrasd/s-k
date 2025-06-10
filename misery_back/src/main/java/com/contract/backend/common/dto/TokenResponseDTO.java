package com.contract.backend.common.dto;

public class TokenResponseDTO {
    private String accessToken;
    public TokenResponseDTO() {}
    public TokenResponseDTO(String accessToken) {
        this.accessToken = accessToken;
    }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
}
