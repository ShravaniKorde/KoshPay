package com.ewallet.wallet_service.dto.response;

import java.time.LocalDateTime;

public class ContactResponse {

    private Long id;
    private String displayName;
    private String upiId;
    private LocalDateTime createdAt;

    public ContactResponse(
            Long id,
            String displayName,
            String upiId,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.displayName = displayName;
        this.upiId = upiId;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUpiId() {
        return upiId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
