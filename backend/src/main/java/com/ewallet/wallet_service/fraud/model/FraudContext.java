package com.ewallet.wallet_service.fraud.model;

import java.math.BigDecimal;

public class FraudContext {

    private Long fromWalletId;
    private Long toWalletId;
    private BigDecimal amount;

    public FraudContext(Long fromWalletId, Long toWalletId, BigDecimal amount) {
        this.fromWalletId = fromWalletId;
        this.toWalletId = toWalletId;
        this.amount = amount;
    }

    public Long getFromWalletId() {
        return fromWalletId;
    }
    public void setFromWalletId(Long fromWalletId) {
        this.fromWalletId = fromWalletId;
    }
    public Long getToWalletId() {
        return toWalletId;
    }
    public void setToWalletId(Long toWalletId) {
        this.toWalletId = toWalletId;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}