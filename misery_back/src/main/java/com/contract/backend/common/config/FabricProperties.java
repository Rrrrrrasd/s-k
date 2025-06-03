package com.contract.backend.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated; // Spring Boot 3.x에서는 jakarta.validation.constraints.*
import jakarta.validation.constraints.NotBlank; // Spring Boot 3.x

@Configuration
@ConfigurationProperties(prefix = "fabric")
@Validated
public class FabricProperties {

    @NotBlank
    private String walletPath;

    @NotBlank
    private String connectionProfilePath;

    @NotBlank
    private String channelName;

    @NotBlank
    private String chaincodeName;

    @NotBlank
    private String mspId;

    @NotBlank
    private String userIdentity;

    // Getters and Setters

    public String getWalletPath() {
        return walletPath;
    }

    public void setWalletPath(String walletPath) {
        this.walletPath = walletPath;
    }

    public String getConnectionProfilePath() {
        return connectionProfilePath;
    }

    public void setConnectionProfilePath(String connectionProfilePath) {
        this.connectionProfilePath = connectionProfilePath;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChaincodeName() {
        return chaincodeName;
    }

    public void setChaincodeName(String chaincodeName) {
        this.chaincodeName = chaincodeName;
    }

    public String getMspId() {
        return mspId;
    }

    public void setMspId(String mspId) {
        this.mspId = mspId;
    }

    public String getUserIdentity() {
        return userIdentity;
    }

    public void setUserIdentity(String userIdentity) {
        this.userIdentity = userIdentity;
    }
}