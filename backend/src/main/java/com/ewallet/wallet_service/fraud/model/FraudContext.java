package com.ewallet.wallet_service.fraud.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FraudContext {

    private Long fromWalletId;
    private Long toWalletId;
    private BigDecimal amount;
    private Long userId;
    private LocalDateTime transactionTime;
    private BigDecimal currentBalance;
    
    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public FraudContext(Long fromWalletId, Long toWalletId, BigDecimal amount) {
        this.fromWalletId = fromWalletId;
        this.toWalletId = toWalletId;
        this.amount = amount;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(LocalDateTime transactionTime) {
        this.transactionTime = transactionTime;
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