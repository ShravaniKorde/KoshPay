package com.ewallet.wallet_service.dto.response;

import com.ewallet.wallet_service.entity.ScheduledPayment;
import com.ewallet.wallet_service.entity.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;

public class ScheduledPaymentResponse {

    private Long id;
    private String senderEmail;
    private String receiverUpiId;
    private BigDecimal amount;
    private Instant scheduledAt;
    private TransactionStatus status;
    private boolean executed;

    public Long getId() {
        return id;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public String getReceiverUpiId() {
        return receiverUpiId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public boolean isExecuted() {
        return executed;
    }

    // constructor
    public ScheduledPaymentResponse(
            Long id,
            String senderEmail,
            String receiverUpiId,
            BigDecimal amount,
            Instant scheduledAt,
            TransactionStatus status,
            boolean executed
    ) {
        this.id = id;
        this.senderEmail = senderEmail;
        this.receiverUpiId = receiverUpiId;
        this.amount = amount;
        this.scheduledAt = scheduledAt;
        this.status = status;
        this.executed = executed;
    }

    public static ScheduledPaymentResponse from(ScheduledPayment payment) {
    return new ScheduledPaymentResponse(
            payment.getId(),
            payment.getSender().getEmail(),
            payment.getReceiver().getUpiId(),
            payment.getAmount(),
            payment.getScheduledAt(),
            payment.getStatus(),
            payment.isExecuted()
        );
    }

}