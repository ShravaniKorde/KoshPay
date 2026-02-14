package com.ewallet.wallet_service.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ContactCreateRequest {

    @NotBlank
    private String displayName;

    @NotBlank
    private String upiId;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }
}
