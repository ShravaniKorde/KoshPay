package com.ewallet.wallet_service.controller;

import com.ewallet.wallet_service.dto.response.UpiIdResponse;
import com.ewallet.wallet_service.entity.User;
import com.ewallet.wallet_service.entity.VirtualPaymentAddress;
import com.ewallet.wallet_service.exception.ResourceNotFoundException;
import com.ewallet.wallet_service.repository.UserRepository;
import com.ewallet.wallet_service.repository.VirtualPaymentAddressRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/upi")
public class UpiController {

    private final UserRepository userRepository;
    private final VirtualPaymentAddressRepository vpaRepository;

    public UpiController(
            UserRepository userRepository,
            VirtualPaymentAddressRepository vpaRepository
    ) {
        this.userRepository = userRepository;
        this.vpaRepository = vpaRepository;
    }

    // =============================
    // SHOW MY UPI ID
    // =============================
    @GetMapping("/me")
    public UpiIdResponse getMyUpiId() {

        // 1️⃣ Get logged-in user's email from JWT
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        // 2️⃣ Fetch User
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        // 3️⃣ Fetch UPI ID
        VirtualPaymentAddress vpa = vpaRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("UPI ID not found"));

        // 4️⃣ Return response
        return new UpiIdResponse(vpa.getUpiId());
    }
}
