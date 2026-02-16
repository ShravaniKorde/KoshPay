package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.entity.*;
import com.ewallet.wallet_service.exception.InsufficientBalanceException;
import com.ewallet.wallet_service.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledPaymentProcessingService {

    private final WalletRepository walletRepository;
    private final ScheduledPaymentRepository scheduledPaymentRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogService auditLogService;
    private final BalanceWebSocketService balanceWebSocketService;
    private final TransactionStatusService statusService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
public void executeSinglePayment(ScheduledPayment payment) {

    Transaction tx = null;

    try {

        Wallet senderWallet = walletRepository
                .findByUserId(payment.getSender().getId())
                .orElseThrow(() -> new RuntimeException("Sender wallet not found"));

        Wallet receiverWallet = walletRepository
                .findByUserId(payment.getReceiver().getUser().getId())
                .orElseThrow(() -> new RuntimeException("Receiver wallet not found"));

        BigDecimal amount = payment.getAmount();
        BigDecimal senderOldBalance = senderWallet.getBalance();

        tx = new Transaction();
        tx.setFromWallet(senderWallet);
        tx.setToWallet(receiverWallet);
        tx.setAmount(amount);
        tx.setTimestamp(Instant.now());

        // 🔹 INITIATED
        statusService.updateStatus(tx, TransactionStatus.INITIATED);
        auditLogService.log(senderWallet.getUser(), "SCHEDULED_TRANSFER", "INITIATED",
                senderOldBalance, senderOldBalance);

        // 🔹 Validation
        if (senderWallet.getId().equals(receiverWallet.getId())) {
            statusService.updateStatus(tx, TransactionStatus.FAILED);
            auditLogService.log(senderWallet.getUser(), "SCHEDULED_TRANSFER", "FAILED",
                    senderOldBalance, senderOldBalance);
            throw new IllegalArgumentException("Cannot transfer to same wallet");
        }

        if (senderWallet.getBalance().compareTo(amount) < 0) {
            statusService.updateStatus(tx, TransactionStatus.FAILED);
            auditLogService.log(senderWallet.getUser(), "SCHEDULED_TRANSFER", "FAILED",
                    senderOldBalance, senderOldBalance);
            throw new InsufficientBalanceException("Insufficient balance");
        }

        // 🔹 PENDING
        statusService.updateStatus(tx, TransactionStatus.PENDING);
        auditLogService.log(senderWallet.getUser(), "SCHEDULED_TRANSFER", "PENDING",
                senderOldBalance, senderOldBalance);

        // 🔹 Balance Update
        senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
        receiverWallet.setBalance(receiverWallet.getBalance().add(amount));

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        // 🔹 SUCCESS
        statusService.updateStatus(tx, TransactionStatus.SUCCESS);
        auditLogService.log(senderWallet.getUser(), "SCHEDULED_TRANSFER", "SUCCESS",
                senderOldBalance, senderWallet.getBalance());

        balanceWebSocketService.publishBalance(senderWallet.getId(), senderWallet.getBalance());
        balanceWebSocketService.publishBalance(receiverWallet.getId(), receiverWallet.getBalance());

        payment.setExecuted(true);
        payment.setStatus(TransactionStatus.SUCCESS);
        payment.setExecutedAt(Instant.now());

    } catch (Exception e) {

        if (tx != null) {
            statusService.updateStatus(tx, TransactionStatus.FAILED);
        }

        auditLogService.log(payment.getSender(), "SCHEDULED_TRANSFER", "FAILED",
                null, null);

        payment.setStatus(TransactionStatus.FAILED);
        payment.setFailureReason(e.getMessage());
    }

    scheduledPaymentRepository.save(payment);
}

}