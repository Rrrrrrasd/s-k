package com.contract.backend.common.Entity;

import com.contract.backend.common.Entity.idClass.FolderContractId;
import jakarta.persistence.*;

@Entity
@IdClass(FolderContractId.class)
@Table(name = "folder_contracts")
public class FolderContractEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "folder_id", nullable = false)
    private FolderEntity folder;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private ContractEntity contract;

    protected FolderContractEntity() {}

    public FolderContractEntity(FolderEntity folder, ContractEntity contract) {
        this.folder   = folder;
        this.contract = contract;
    }

    // getters & setters …

    public FolderEntity getFolder() {
        return folder;
    }

    public void setFolder(FolderEntity folder) {
        this.folder = folder;
    }

    public ContractEntity getContract() {
        return contract;
    }

    public void setContract(ContractEntity contract) {
        this.contract = contract;
    }
}
