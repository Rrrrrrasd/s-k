package com.contract.backend.common.Entity;

import com.contract.backend.common.Entity.enumm.PartyRole;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract_parties",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_party_contract",
                columnNames = {"contract_id", "party_id"}),
        indexes = {
                @Index(name = "idx_cp_contract", columnList = "contract_id"),
                @Index(name = "idx_cp_party", columnList = "party_id")
        })
public class ContractPartyEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private ContractEntity contract;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "party_id", nullable = false)
    private UserEntity party;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartyRole role;

    @Column(name = "invited_at", nullable = false)
    private LocalDateTime invitedAt;

    protected ContractPartyEntity() {}

    public ContractPartyEntity(
            ContractEntity contract,
            UserEntity party,
            PartyRole role
    ) {
        this.contract = contract;
        this.party   = party;
        this.role    = role;
        this.invitedAt = LocalDateTime.now();
    }

    // getters & setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ContractEntity getContract() {
        return contract;
    }

    public void setContract(ContractEntity contract) {
        this.contract = contract;
    }

    public UserEntity getParty() {
        return party;
    }

    public void setParty(UserEntity party) {
        this.party = party;
    }

    public PartyRole getRole() {
        return role;
    }

    public void setRole(PartyRole role) {
        this.role = role;
    }

    public LocalDateTime getInvitedAt() {
        return invitedAt;
    }

    public void setInvitedAt(LocalDateTime invitedAt) {
        this.invitedAt = invitedAt;
    }


}
