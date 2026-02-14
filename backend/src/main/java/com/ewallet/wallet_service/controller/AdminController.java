package com.ewallet.wallet_service.controller;

import com.ewallet.wallet_service.dto.response.AdminSummaryResponse;
import com.ewallet.wallet_service.dto.response.AdminTransactionResponse;
import com.ewallet.wallet_service.dto.response.TransactionStatusDistributionResponse;
import com.ewallet.wallet_service.entity.AuditLog;
import com.ewallet.wallet_service.entity.Transaction;
import com.ewallet.wallet_service.repository.AuditLogRepository;
import com.ewallet.wallet_service.repository.TransactionRepository;
import com.ewallet.wallet_service.repository.VirtualPaymentAddressRepository;
import com.ewallet.wallet_service.service.AdminAnalyticsService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminAnalyticsService analyticsService;
    private final TransactionRepository transactionRepository;
    private final AuditLogRepository auditLogRepository;
    private final VirtualPaymentAddressRepository vpaRepository;

    // =====================================================
    // SUMMARY
    // =====================================================
    @GetMapping("/summary")
    public ResponseEntity<AdminSummaryResponse> getSummary() {
        return ResponseEntity.ok(analyticsService.getSummary());
    }

    // =====================================================
    // STATUS DISTRIBUTION (Lifecycle States)
    // =====================================================
    @GetMapping("/status-distribution")
    public ResponseEntity<TransactionStatusDistributionResponse> getStatusDistribution() {
        return ResponseEntity.ok(analyticsService.getStatusDistribution());
    }

    // =====================================================
    // ALL TRANSACTIONS (RETURNING UPI IDs)
    // =====================================================
    @GetMapping("/transactions")
    @Transactional(readOnly = true)
    public ResponseEntity<List<AdminTransactionResponse>> getAllTransactions() {

        List<Transaction> transactions = transactionRepository.findAll();

        List<AdminTransactionResponse> result = transactions.stream().map(tx -> {

            Long fromUserId = tx.getFromWallet().getUser().getId();
            Long toUserId = tx.getToWallet().getUser().getId();

            String fromUpi = vpaRepository.findByUserId(fromUserId)
                    .map(vpa -> vpa.getUpiId())
                    .orElse("N/A");

            String toUpi = vpaRepository.findByUserId(toUserId)
                    .map(vpa -> vpa.getUpiId())
                    .orElse("N/A");

            return new AdminTransactionResponse(
                    tx.getId(),
                    fromUpi,
                    toUpi,
                    tx.getAmount(),
                    tx.getStatus().name(),
                    tx.getTimestamp()
            );

        }).toList();

        return ResponseEntity.ok(result);
    }

    // =====================================================
    // AUDIT LOGS (NO BALANCE EXPOSURE IN UI LAYER)
    // =====================================================
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(auditLogRepository.findAll());
    }

}
