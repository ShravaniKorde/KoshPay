package com.ewallet.wallet_service.dto.response;

public class UpiIdResponse {

    private final String upiId;

    public UpiIdResponse(String upiId) {
        this.upiId = upiId;
    }

    public String getUpiId() {
        return upiId;
    }
}
