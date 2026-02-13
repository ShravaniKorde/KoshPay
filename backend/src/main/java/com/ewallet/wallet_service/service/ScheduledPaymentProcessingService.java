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

            // 🔹 Fetch wallets
            Wallet senderWallet = walletRepository
                    .findByUserId(payment.getSender().getId())
                    .orElseThrow(() -> new RuntimeException("Sender wallet not found"));

            Wallet receiverWallet = walletRepository
                    .findByUserId(payment.getReceiver().getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Receiver wallet not found"));

            BigDecimal amount = payment.getAmount();
            BigDecimal senderOldBalance = senderWallet.getBalance();

            // 🔹 Create transaction record
            tx = new Transaction();
            tx.setFromWallet(senderWallet);
            tx.setToWallet(receiverWallet);
            tx.setAmount(amount);
            tx.setTimestamp(Instant.now());

            statusService.updateStatus(tx, TransactionStatus.INITIATED);

            // 🔹 Validation
            if (senderWallet.getId().equals(receiverWallet.getId())) {
                statusService.updateStatus(tx, TransactionStatus.FAILED);
                throw new IllegalArgumentException("Cannot transfer to same wallet");
            }

            if (senderWallet.getBalance().compareTo(amount) < 0) {
                statusService.updateStatus(tx, TransactionStatus.FAILED);
                throw new InsufficientBalanceException("Insufficient balance");
            }

            // 🔹 Pending state
            statusService.updateStatus(tx, TransactionStatus.PENDING);

            // 🔹 Perform balance update
            senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
            receiverWallet.setBalance(receiverWallet.getBalance().add(amount));

            walletRepository.save(senderWallet);
            walletRepository.save(receiverWallet);

            // 🔹 Success
            statusService.updateStatus(tx, TransactionStatus.SUCCESS);

            // 🔹 Audit Log
            auditLogService.log(
                    senderWallet.getUser(),
                    "SCHEDULED_TRANSFER",
                    "SUCCESS",
                    senderOldBalance,
                    senderWallet.getBalance()
            );

            // 🔹 WebSocket updates
            balanceWebSocketService.publishBalance(
                    senderWallet.getId(),
                    senderWallet.getBalance()
            );

            balanceWebSocketService.publishBalance(
                    receiverWallet.getId(),
                    receiverWallet.getBalance()
            );

            // 🔹 Mark scheduled payment success
            payment.setExecuted(true);
            payment.setStatus(TransactionStatus.SUCCESS);
            payment.setExecutedAt(Instant.now());

            log.info("Scheduled payment executed successfully: {}", payment.getId());

        } catch (Exception e) {

            if (tx != null) {
                statusService.updateStatus(tx, TransactionStatus.FAILED);
            }

            payment.setStatus(TransactionStatus.FAILED);
            payment.setFailureReason(e.getMessage());

            log.error("Scheduled payment failed: {}", payment.getId(), e);
        }

        scheduledPaymentRepository.save(payment);
    }
}