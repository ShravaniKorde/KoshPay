package com.ewallet.wallet_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
public class AdminTransactionResponse {

    private Long transactionId;

    private String fromUpiId;
    private String toUpiId;

    private BigDecimal amount;

    private String status;

    private Instant timestamp;
}
