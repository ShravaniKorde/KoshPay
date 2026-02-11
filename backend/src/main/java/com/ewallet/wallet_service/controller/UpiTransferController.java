package com.ewallet.wallet_service.controller;

import com.ewallet.wallet_service.dto.request.UpiTransferRequest;
import com.ewallet.wallet_service.entity.Wallet;
import com.ewallet.wallet_service.service.UpiResolverService;
import com.ewallet.wallet_service.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ewallet.wallet_service.dto.response.OtpResponse;

@RestController
@RequestMapping("/api/upi")
public class UpiTransferController {

    private final WalletService walletService;
    private final UpiResolverService upiResolverService;

    public UpiTransferController(
            WalletService walletService,
            UpiResolverService upiResolverService
    ) {
        this.walletService = walletService;
        this.upiResolverService = upiResolverService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transferViaUpi(
            @Valid @RequestBody UpiTransferRequest request
    ) {

        Wallet receiverWallet =
                upiResolverService.resolveToWallet(request.getToUpiId());

    Object result = walletService.transfer(
            receiverWallet.getId(),
            request.getAmount(),
            request.getPin(),
            request.getOtp()
    );

        // If the service returned an OtpResponse, send it to frontend
        if (result instanceof OtpResponse) {
        // This sends back the JSON with the OTP code
        return ResponseEntity.ok(result); 
        }

        return ResponseEntity.ok("UPI transfer successful");
    }
}
