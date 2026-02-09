package com.ewallet.wallet_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class UpiTransferRequest {

    @NotBlank
    private String toUpiId;

    @NotNull
    private BigDecimal amount;

    public String getToUpiId() {
        return toUpiId;
    }

    public void setToUpiId(String toUpiId) {
        this.toUpiId = toUpiId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
