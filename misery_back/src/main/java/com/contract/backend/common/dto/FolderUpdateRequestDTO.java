// FolderUpdateRequestDTO.java
package com.contract.backend.common.dto;

public class FolderUpdateRequestDTO {
    private String name;
    private Long parentId; // 폴더 이동용

    public FolderUpdateRequestDTO() {}

    public FolderUpdateRequestDTO(String name, Long parentId) {
        this.name = name;
        this.parentId = parentId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
}