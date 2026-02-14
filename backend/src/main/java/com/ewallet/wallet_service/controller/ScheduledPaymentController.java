package com.ewallet.wallet_service.controller;

import com.ewallet.wallet_service.dto.request.SchedulePaymentRequest;
import com.ewallet.wallet_service.dto.response.ScheduledPaymentResponse;
import com.ewallet.wallet_service.entity.ScheduledPayment;
import com.ewallet.wallet_service.service.ScheduledPaymentService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduled-payments")
@RequiredArgsConstructor
public class ScheduledPaymentController {

    private final ScheduledPaymentService scheduledPaymentService;

    @PostMapping
    public ResponseEntity<?> createSchedule(
            @RequestBody SchedulePaymentRequest request,
            Authentication authentication
    ) {

        String email = authentication.getName();

        ScheduledPayment payment =
                scheduledPaymentService.createSchedule(
                        email,
                        request.getReceiverUpiId(),
                        request.getAmount(),
                        request.getScheduledAt()
                );

        ScheduledPaymentResponse response =
            new ScheduledPaymentResponse(
                    payment.getId(),
                    payment.getSender().getEmail(),
                    payment.getReceiver().getUpiId(),
                    payment.getAmount(),
                    payment.getScheduledAt(),
                    payment.getStatus(),
                    payment.isExecuted()
                );        

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ScheduledPaymentResponse>> getUserSchedules(
            Authentication authentication
    ) {

        String email = authentication.getName();

        List<ScheduledPaymentResponse> responses =
            scheduledPaymentService
                    .getUserScheduledPayments(email)
                    .stream()
                    .map(ScheduledPaymentResponse::from)
                    .toList();

        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
        public ResponseEntity<?> updateSchedule(
                @PathVariable Long id,
                @RequestBody SchedulePaymentRequest request,
                Authentication authentication
        ) {
        ScheduledPayment updated =
                scheduledPaymentService.updateSchedule(
                        id,
                        authentication.getName(),
                        request.getAmount(),
                        request.getScheduledAt()
                );

        return ResponseEntity.ok(ScheduledPaymentResponse.from(updated));
        }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelSchedule(
            @PathVariable Long id,
            Authentication authentication
    ) {

        scheduledPaymentService.cancelSchedule(id, authentication.getName());

        return ResponseEntity.ok("Scheduled payment cancelled");
    }
}