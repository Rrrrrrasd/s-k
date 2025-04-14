package com.contract.backend.common.dto;

import com.yubico.webauthn.data.PublicKeyCredentialRequestOptions;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartLoginResponseDTO {
    private PublicKeyCredentialRequestOptions publicKey;
    private String username;
}
