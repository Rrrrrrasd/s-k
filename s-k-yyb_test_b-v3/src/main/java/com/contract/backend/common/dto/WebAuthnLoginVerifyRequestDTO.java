package com.contract.backend.common.dto;

import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;

public class WebAuthnLoginVerifyRequestDTO {
    private AssertionRequest request;
    private PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> response;

    public AssertionRequest getRequest() {
        return request;
    }

    public void setRequest(AssertionRequest request) {
        this.request = request;
    }

    public PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> getResponse() {
        return response;
    }

    public void setResponse(PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> response) {
        this.response = response;
    }
}