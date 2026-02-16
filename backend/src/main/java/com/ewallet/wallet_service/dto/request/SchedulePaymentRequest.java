package com.ewallet.wallet_service.dto.request;

import java.math.BigDecimal;
import java.time.Instant;

public class SchedulePaymentRequest {

    private String receiverUpiId;
    private BigDecimal amount;
    private Instant scheduledAt;
    
    public String getReceiverUpiId() {
        return receiverUpiId;
    }
    public void setReceiverUpiId(String receiverUpiId) {
        this.receiverUpiId = receiverUpiId;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public Instant getScheduledAt() {
        return scheduledAt;
    }
    public void setScheduledAt(Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
    }
}