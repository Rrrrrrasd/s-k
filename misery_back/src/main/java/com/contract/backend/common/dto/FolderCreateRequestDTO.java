// FolderCreateRequestDTO.java
package com.contract.backend.common.dto;

public class FolderCreateRequestDTO {
    private String name;
    private Long parentId; // null이면 루트 폴더

    public FolderCreateRequestDTO() {}

    public FolderCreateRequestDTO(String name, Long parentId) {
        this.name = name;
        this.parentId = parentId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
}