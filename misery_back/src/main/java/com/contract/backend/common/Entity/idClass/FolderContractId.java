package com.contract.backend.common.Entity.idClass;

import java.io.Serializable;
import java.util.Objects;

public class FolderContractId implements Serializable {
    private Long folder;
    private Long contract;

    public FolderContractId() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FolderContractId)) return false;
        FolderContractId that = (FolderContractId) o;
        return Objects.equals(folder, that.folder)
                && Objects.equals(contract, that.contract);
    }

    @Override
    public int hashCode() {
        return Objects.hash(folder, contract);
    }
}
