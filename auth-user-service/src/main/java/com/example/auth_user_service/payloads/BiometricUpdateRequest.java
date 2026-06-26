package com.example.auth_user_service.payloads;

public class BiometricUpdateRequest {
    private boolean enableBiometric;

    public boolean getEnableBiometric() { return enableBiometric; }
    public void setEnableBiometric(boolean enableBiometric) { this.enableBiometric = enableBiometric; }
}
