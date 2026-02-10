package com.ewallet.wallet_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
public class TransactionResponse {

    private Long transactionId;

    // DEBIT or CREDIT (from current user's perspective)
    private String type;

    private BigDecimal amount;

    private Long counterpartyWalletId;

    private Instant timestamp;

    private String status; // New field
}
