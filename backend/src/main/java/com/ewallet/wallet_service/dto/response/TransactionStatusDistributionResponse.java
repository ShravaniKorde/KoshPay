package com.ewallet.wallet_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionStatusDistributionResponse {

    private long initiated;
    private long pending;
    private long success;
    private long failed;
}
