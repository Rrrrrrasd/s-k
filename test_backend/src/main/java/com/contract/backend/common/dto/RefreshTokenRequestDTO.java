package com.contract.backend.common.dto;

public class RefreshTokenRequestDTO {
    private String refreshToken;
    public RefreshTokenRequestDTO() {}
    public RefreshTokenRequestDTO(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
