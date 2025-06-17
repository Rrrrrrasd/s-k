// FolderResponseDTO.java
package com.contract.backend.common.dto;

import java.time.LocalDateTime;
import java.util.List;

public class FolderResponseDTO {
    private Long id;
    private String name;
    private String path;
    private Long parentId;
    private String parentName;
    private LocalDateTime createdAt;
    private UserResponseDTO createdBy;
    private List<FolderResponseDTO> children;
    private List<ContractListDTO> contracts;
    private int childrenCount;
    private int contractsCount;

    public FolderResponseDTO() {}

    public FolderResponseDTO(Long id, String name, String path, Long parentId, 
                           LocalDateTime createdAt, UserResponseDTO createdBy) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.parentId = parentId;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    
    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public UserResponseDTO getCreatedBy() { return createdBy; }
    public void setCreatedBy(UserResponseDTO createdBy) { this.createdBy = createdBy; }
    
    public List<FolderResponseDTO> getChildren() { return children; }
    public void setChildren(List<FolderResponseDTO> children) { this.children = children; }
    
    public List<ContractListDTO> getContracts() { return contracts; }
    public void setContracts(List<ContractListDTO> contracts) { this.contracts = contracts; }
    
    public int getChildrenCount() { return childrenCount; }
    public void setChildrenCount(int childrenCount) { this.childrenCount = childrenCount; }
    
    public int getContractsCount() { return contractsCount; }
    public void setContractsCount(int contractsCount) { this.contractsCount = contractsCount; }
}