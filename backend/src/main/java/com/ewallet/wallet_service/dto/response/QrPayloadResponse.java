package com.ewallet.wallet_service.dto.response;

public class QrPayloadResponse {

    private final String payload;

    public QrPayloadResponse(String payload) {
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }
}
