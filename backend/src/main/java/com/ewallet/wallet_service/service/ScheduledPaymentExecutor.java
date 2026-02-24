package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.entity.ScheduledPayment;
import com.ewallet.wallet_service.repository.ScheduledPaymentRepository;
import com.ewallet.wallet_service.entity.TransactionStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledPaymentExecutor {

    private final ScheduledPaymentRepository scheduledPaymentRepository;
    private final ScheduledPaymentProcessingService processingService;

    // FIX: was 60000 (1 minute) — payments could be up to 60s late.
    // Now runs every 10s. Safe because findPendingPayments() only returns
    // unexecuted PENDING rows so the same payment never runs twice.
    @Scheduled(fixedRate = 10000)
    public void processScheduledPayments() {

        List<ScheduledPayment> pendingPayments =
                scheduledPaymentRepository
                        .findPendingPayments(Instant.now(),  TransactionStatus.PENDING);

        if (!pendingPayments.isEmpty()) {
            log.info("Processing {} scheduled payment(s)", pendingPayments.size());
        }

        for (ScheduledPayment payment : pendingPayments) {
            processingService.executeSinglePayment(payment);
        }
    }
}