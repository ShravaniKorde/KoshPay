package com.ewallet.wallet_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AdminAnalyticsResponse {

    private long totalUsers;
    private long totalTransactions;

    private BigDecimal totalVolume;

    private long successCount;
    private long failedCount;
    private long pendingCount;

    private long todayTransactions;
    private BigDecimal todayVolume;
}
