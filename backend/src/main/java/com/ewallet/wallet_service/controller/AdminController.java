package com.ewallet.wallet_service.controller;

import com.ewallet.wallet_service.dto.response.AdminSummaryResponse;
import com.ewallet.wallet_service.dto.response.AdminTransactionResponse;
import com.ewallet.wallet_service.dto.response.TransactionStatusDistributionResponse;
import com.ewallet.wallet_service.entity.AuditLog;
import com.ewallet.wallet_service.repository.AuditLogRepository;
import com.ewallet.wallet_service.repository.TransactionRepository;
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
    // ALL TRANSACTIONS (NO BALANCE EXPOSURE)
    // =====================================================
    @GetMapping("/transactions")
    @Transactional(readOnly = true) // 🔥 FIXES LAZY LOADING ERROR
    public ResponseEntity<List<AdminTransactionResponse>> getAllTransactions() {

        List<AdminTransactionResponse> result =
                transactionRepository.findAll()
                        .stream()
                        .map(tx -> new AdminTransactionResponse(
                                tx.getId(),
                                tx.getFromWallet().getId(), // wallet ID only
                                tx.getToWallet().getId(),   // wallet ID only
                                tx.getAmount(),
                                tx.getStatus().name(),      // ✅ lifecycle state
                                tx.getTimestamp()
                        ))
                        .toList();

        return ResponseEntity.ok(result);
    }

    // =====================================================
    // AUDIT LOGS (NO BALANCE FIELD EXPOSED TO ADMIN)
    // =====================================================
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(auditLogRepository.findAll());
    }

}
