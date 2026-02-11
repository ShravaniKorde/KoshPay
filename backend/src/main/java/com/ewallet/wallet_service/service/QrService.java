package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.entity.User;
import com.ewallet.wallet_service.entity.VirtualPaymentAddress;
import com.ewallet.wallet_service.exception.ResourceNotFoundException;
import com.ewallet.wallet_service.repository.UserRepository;
import com.ewallet.wallet_service.repository.VirtualPaymentAddressRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class QrService {

    private final UserRepository userRepository;
    private final VirtualPaymentAddressRepository vpaRepository;

    public QrService(
            UserRepository userRepository,
            VirtualPaymentAddressRepository vpaRepository
    ) {
        this.userRepository = userRepository;
        this.vpaRepository = vpaRepository;
    }

    public String generateQrPayload(BigDecimal amount) {

        // 1️⃣ Get logged-in user
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        // 2️⃣ Fetch UPI ID
        VirtualPaymentAddress vpa = vpaRepository
                .findByUserId(user.getId())
                .filter(VirtualPaymentAddress::isActive)
                .orElseThrow(() ->
                        new ResourceNotFoundException("UPI ID not found"));

        String upiId = vpa.getUpiId();
        String name = user.getName();

        // URL encode name (important for spaces/special chars)
        String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);

        // 3️⃣ Build payload
        StringBuilder payload = new StringBuilder();
        payload.append("upi://pay?");
        payload.append("pa=").append(upiId);
        payload.append("&pn=").append(encodedName);
        payload.append("&cu=INR");

        // Optional amount
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            payload.append("&am=").append(amount);
        }

        return payload.toString();
    }
}
