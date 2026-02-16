package com.ewallet.wallet_service.controller;

import com.ewallet.wallet_service.dto.response.QrPayloadResponse;
import com.ewallet.wallet_service.dto.response.UpiIdResponse;
import com.ewallet.wallet_service.entity.User;
import com.ewallet.wallet_service.entity.VirtualPaymentAddress;
import com.ewallet.wallet_service.exception.ResourceNotFoundException;
import com.ewallet.wallet_service.repository.UserRepository;
import com.ewallet.wallet_service.repository.VirtualPaymentAddressRepository;
import com.ewallet.wallet_service.service.QrService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/upi")
public class UpiController {

    private final UserRepository userRepository;
    private final VirtualPaymentAddressRepository vpaRepository;
    private final QrService qrService;

    public UpiController(
            UserRepository userRepository,
            VirtualPaymentAddressRepository vpaRepository,
            QrService qrService
    ) {
        this.userRepository = userRepository;
        this.vpaRepository = vpaRepository;
        this.qrService = qrService;
    }

    // =============================
    // SHOW MY UPI ID
    // =============================
    @GetMapping("/me")
    public UpiIdResponse getMyUpiId() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        VirtualPaymentAddress vpa = vpaRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("UPI ID not found"));

        return new UpiIdResponse(vpa.getUpiId());
    }

    // =============================
    // GENERATE QR PAYLOAD
    // =============================
    @GetMapping("/qr")
    public QrPayloadResponse generateQr(
            @RequestParam(required = false) BigDecimal amount
    ) {
        String payload = qrService.generateQrPayload(amount);
        return new QrPayloadResponse(payload);
    }
}
