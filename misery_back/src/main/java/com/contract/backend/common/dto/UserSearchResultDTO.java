package com.contract.backend.common.dto;

public class UserSearchResultDTO {
    private Long id;
    private String uuid;
    private String username;
    private String email;

    public UserSearchResultDTO() {}

    public UserSearchResultDTO(Long id, String uuid, String username, String email) {
        this.id = id;
        this.uuid = uuid;
        this.username = username;
        this.email = email;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}