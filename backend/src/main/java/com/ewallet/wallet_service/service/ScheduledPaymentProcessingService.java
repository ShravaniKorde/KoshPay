package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.entity.ScheduledPayment;
import com.ewallet.wallet_service.entity.TransactionStatus;
import com.ewallet.wallet_service.entity.Wallet;
import com.ewallet.wallet_service.exception.InsufficientBalanceException;
import com.ewallet.wallet_service.repository.ScheduledPaymentRepository;
import com.ewallet.wallet_service.repository.WalletRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.Instant;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledPaymentProcessingService {

    private final WalletRepository walletRepository;
    private final ScheduledPaymentRepository scheduledPaymentRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeSinglePayment(ScheduledPayment payment) {

        try {

            // 🔹 Fetch sender wallet
            Wallet senderWallet = walletRepository
                    .findByUserId(payment.getSender().getId())
                    .orElseThrow(() -> new RuntimeException("Sender wallet not found"));

            // 🔹 Fetch receiver wallet (VPA → User → Wallet)
            Wallet receiverWallet = walletRepository
                    .findByUserId(payment.getReceiver().getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Receiver wallet not found"));

            BigDecimal amount = payment.getAmount();

            // 🔹 Prevent self transfer
            if (senderWallet.getId().equals(receiverWallet.getId())) {
                throw new IllegalArgumentException("Cannot transfer to same wallet");
            }

            // 🔹 Check balance
            if (senderWallet.getBalance().compareTo(amount) < 0) {
                throw new InsufficientBalanceException("Insufficient balance");
            }

            // 🔹 Perform balance update
            senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
            receiverWallet.setBalance(receiverWallet.getBalance().add(amount));

            walletRepository.save(senderWallet);
            walletRepository.save(receiverWallet);

            // 🔹 Mark scheduled payment success
            payment.setExecuted(true);
            payment.setStatus(TransactionStatus.SUCCESS);
            payment.setExecutedAt(Instant.now());

            log.info("Scheduled payment executed successfully: {}", payment.getId());

        } catch (Exception e) {

            payment.setStatus(TransactionStatus.FAILED);
            payment.setFailureReason(e.getMessage());

            log.error("Scheduled payment failed: {}", payment.getId(), e);
        }

        scheduledPaymentRepository.save(payment);
    }
}