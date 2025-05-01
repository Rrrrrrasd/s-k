package com.contract.backend.common.dto;

import com.yubico.webauthn.FinishRegistrationOptions;

public class WebAuthnRegisterVerifyRequestDTO {
    private FinishRegistrationOptions options;
    private String deviceName;

    public WebAuthnRegisterVerifyRequestDTO() {}

    public FinishRegistrationOptions getOptions() {
        return options;
    }

    public void setOptions(FinishRegistrationOptions options) {
        this.options = options;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
