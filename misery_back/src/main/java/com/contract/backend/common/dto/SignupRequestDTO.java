package com.contract.backend.common.dto;

public class SignupRequestDTO {
    private String username;
    private String password;
    private String email;

    public SignupRequestDTO() {}

    public SignupRequestDTO(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public String getUsername() {return username;}
    public String getPassword() {return password;}
    public String getEmail() {return email;}
}

