package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.dto.response.AdminSummaryResponse;
import com.ewallet.wallet_service.dto.response.TransactionStatusDistributionResponse;
import com.ewallet.wallet_service.entity.TransactionStatus;
import com.ewallet.wallet_service.repository.AuditLogRepository;
import com.ewallet.wallet_service.repository.TransactionRepository;
import com.ewallet.wallet_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AdminAnalyticsService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminSummaryResponse getSummary() {

        long totalUsers = userRepository.count();
        long totalTransactions = transactionRepository.count();

        BigDecimal totalVolume =
                transactionRepository.sumSuccessfulTransactionVolume();

        long fraudBlocked =
                auditLogRepository.countByActionTypeAndStatus(
                        "TRANSFER",
                        "FRAUD_BLOCK"
                );

        return new AdminSummaryResponse(
                totalUsers,
                totalTransactions,
                totalVolume,
                fraudBlocked
        );
    }

    public TransactionStatusDistributionResponse getStatusDistribution() {

        long initiated =
                transactionRepository.countByStatus(TransactionStatus.INITIATED);

        long pending =
                transactionRepository.countByStatus(TransactionStatus.PENDING);

        long success =
                transactionRepository.countByStatus(TransactionStatus.SUCCESS);

        long failed =
                transactionRepository.countByStatus(TransactionStatus.FAILED);

        return new TransactionStatusDistributionResponse(
                initiated,
                pending,
                success,
                failed
        );
    }
}
