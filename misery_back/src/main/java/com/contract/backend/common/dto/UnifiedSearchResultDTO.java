package com.contract.backend.common.dto;

import java.time.LocalDateTime;

public class UnifiedSearchResultDTO {
    private Long id;
    private String name;
    private String type; 
    private String status; 
    private LocalDateTime createdAt;

    public UnifiedSearchResultDTO(Long id, String name, String type, String status, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}