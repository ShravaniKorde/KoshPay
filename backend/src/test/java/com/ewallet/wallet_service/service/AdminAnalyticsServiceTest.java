package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.dto.response.AdminSummaryResponse;
import com.ewallet.wallet_service.dto.response.TransactionStatusDistributionResponse;
import com.ewallet.wallet_service.entity.TransactionStatus;
import com.ewallet.wallet_service.repository.AuditLogRepository;
import com.ewallet.wallet_service.repository.TransactionRepository;
import com.ewallet.wallet_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminAnalyticsServiceTest {

    private UserRepository userRepository;
    private TransactionRepository transactionRepository;
    private AuditLogRepository auditLogRepository;
    private AdminAnalyticsService adminAnalyticsService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        auditLogRepository = mock(AuditLogRepository.class);
        
        adminAnalyticsService = new AdminAnalyticsService(
            userRepository, 
            transactionRepository, 
            auditLogRepository
        );
    }

    @Test
    void getSummary_Success() {
        when(userRepository.count()).thenReturn(100L);
        when(transactionRepository.count()).thenReturn(500L);
        when(transactionRepository.sumSuccessfulTransactionVolume()).thenReturn(new BigDecimal("50000.00"));
        when(auditLogRepository.countByActionTypeAndStatus("TRANSFER", "FRAUD_BLOCK")).thenReturn(5L);

        AdminSummaryResponse summary = adminAnalyticsService.getSummary();

        assertNotNull(summary);
        assertEquals(100L, summary.getTotalUsers());
        assertEquals(500L, summary.getTotalTransactions());
        assertEquals(new BigDecimal("50000.00"), summary.getTotalVolume());
        assertEquals(5L, summary.getFraudBlockedCount()); 
    }

    @Test
    void getStatusDistribution_Success() {
        when(transactionRepository.countByStatus(TransactionStatus.INITIATED)).thenReturn(10L);
        when(transactionRepository.countByStatus(TransactionStatus.PENDING)).thenReturn(20L);
        when(transactionRepository.countByStatus(TransactionStatus.SUCCESS)).thenReturn(100L);
        when(transactionRepository.countByStatus(TransactionStatus.FAILED)).thenReturn(5L);

        TransactionStatusDistributionResponse distribution = adminAnalyticsService.getStatusDistribution();

        assertEquals(10L, distribution.getInitiated());
        assertEquals(20L, distribution.getPending());
        assertEquals(100L, distribution.getSuccess());
        assertEquals(5L, distribution.getFailed());
    }
}