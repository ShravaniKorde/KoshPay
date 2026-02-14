package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.entity.ScheduledPayment;
import com.ewallet.wallet_service.entity.TransactionStatus;
import com.ewallet.wallet_service.entity.User;
import com.ewallet.wallet_service.entity.VirtualPaymentAddress;
import com.ewallet.wallet_service.repository.ScheduledPaymentRepository;
import com.ewallet.wallet_service.repository.UserRepository;
import com.ewallet.wallet_service.repository.VirtualPaymentAddressRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduledPaymentService {

    private final ScheduledPaymentRepository scheduledPaymentRepository;
    private final UserRepository userRepository;
    private final VirtualPaymentAddressRepository vpaRepository;

    @Transactional
    public ScheduledPayment updateSchedule(
            Long id,
            String email,
            BigDecimal amount,
            Instant scheduledAt
    ) {

        ScheduledPayment payment = scheduledPaymentRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        if (!payment.getSender().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }

        if (payment.isExecuted()) {
            throw new RuntimeException("Cannot edit executed payment");
        }

        payment.setAmount(amount);
        payment.setScheduledAt(scheduledAt);

        return scheduledPaymentRepository.save(payment);
    }


    public ScheduledPayment createSchedule(
            String senderEmail,
            String receiverUpiId,
            BigDecimal amount,
            Instant scheduledAt
    ) {

        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        VirtualPaymentAddress receiver =
                vpaRepository.findByUpiId(receiverUpiId)
                        .orElseThrow(() -> new RuntimeException("Receiver not found"));

        ScheduledPayment payment = new ScheduledPayment();
        payment.setSender(sender);
        payment.setReceiver(receiver);
        payment.setAmount(amount);
        payment.setScheduledAt(scheduledAt);
        payment.setStatus(TransactionStatus.PENDING);
        payment.setExecuted(false);
        payment.setCreatedAt(Instant.now());

        return scheduledPaymentRepository.save(payment);
    }

    public List<ScheduledPayment> getUserScheduledPayments(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return scheduledPaymentRepository.findBySender(user);
    }

    public void cancelSchedule(Long scheduleId, String email) {

        ScheduledPayment payment =
                scheduledPaymentRepository.findById(scheduleId)
                        .orElseThrow(() -> new RuntimeException("Schedule not found"));

        if (!payment.getSender().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized cancellation");
        }

        if (payment.isExecuted()) {
            throw new RuntimeException("Already executed");
        }

        payment.setStatus(TransactionStatus.FAILED);
        payment.setFailureReason("Cancelled by user");
        scheduledPaymentRepository.save(payment);
    }

    

    
}