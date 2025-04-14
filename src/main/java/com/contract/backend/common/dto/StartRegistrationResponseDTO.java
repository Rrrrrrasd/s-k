package com.contract.backend.common.dto;

import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartRegistrationResponseDTO {
    private PublicKeyCredentialCreationOptions publicKey;
    private String username;
}
