package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.entity.ScheduledPayment;
import com.ewallet.wallet_service.repository.ScheduledPaymentRepository;

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

    @Scheduled(fixedRate = 60000)
    public void processScheduledPayments() {

        List<ScheduledPayment> pendingPayments =
                scheduledPaymentRepository
                        .findPendingPayments(Instant.now());

        for (ScheduledPayment payment : pendingPayments) {
            processingService.executeSinglePayment(payment);
        }
    }
}