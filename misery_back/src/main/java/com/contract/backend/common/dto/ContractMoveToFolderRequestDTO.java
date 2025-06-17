// ContractMoveToFolderRequestDTO.java
package com.contract.backend.common.dto;

public class ContractMoveToFolderRequestDTO {
    private Long contractId;
    private Long folderId; // null이면 루트로 이동

    public ContractMoveToFolderRequestDTO() {}

    public ContractMoveToFolderRequestDTO(Long contractId, Long folderId) {
        this.contractId = contractId;
        this.folderId = folderId;
    }

    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
    
    public Long getFolderId() { return folderId; }
    public void setFolderId(Long folderId) { this.folderId = folderId; }
}