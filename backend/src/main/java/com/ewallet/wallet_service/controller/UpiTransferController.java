package com.ewallet.wallet_service.controller;

import com.ewallet.wallet_service.dto.request.UpiTransferRequest;
import com.ewallet.wallet_service.entity.Wallet;
import com.ewallet.wallet_service.service.UpiResolverService;
import com.ewallet.wallet_service.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

        // 🔑 Reuse EXISTING transfer logic
        walletService.transfer(
                receiverWallet.getId(),
                request.getAmount()
        );

        return ResponseEntity.ok("UPI transfer successful");
    }
}
