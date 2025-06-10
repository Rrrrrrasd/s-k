package com.contract.backend.common.dto;

import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;

public class WebAuthnRegisterVerifyRequestDTO {
    private PublicKeyCredentialCreationOptions request;
    private PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> response;
    private String deviceName;

    public WebAuthnRegisterVerifyRequestDTO() {}

    public WebAuthnRegisterVerifyRequestDTO(
            PublicKeyCredentialCreationOptions request,
            PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> response,
            String deviceName
    ) {
        this.request = request;
        this.response = response;
        this.deviceName = deviceName;
    }

    public PublicKeyCredentialCreationOptions getRequest() {
        return request;
    }

    public void setRequest(PublicKeyCredentialCreationOptions request) {
        this.request = request;
    }

    public PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> getResponse() {
        return response;
    }

    public void setResponse(PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> response) {
        this.response = response;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
