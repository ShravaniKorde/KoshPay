package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.entity.VirtualPaymentAddress;
import com.ewallet.wallet_service.entity.Wallet;
import com.ewallet.wallet_service.exception.ResourceNotFoundException;
import com.ewallet.wallet_service.repository.VirtualPaymentAddressRepository;
import com.ewallet.wallet_service.repository.WalletRepository;
import org.springframework.stereotype.Service;

@Service
public class UpiResolverService {

    private final VirtualPaymentAddressRepository vpaRepository;
    private final WalletRepository walletRepository;

    public UpiResolverService(
            VirtualPaymentAddressRepository vpaRepository,
            WalletRepository walletRepository
    ) {
        this.vpaRepository = vpaRepository;
        this.walletRepository = walletRepository;
    }

    public Wallet resolveToWallet(String upiId) {

        VirtualPaymentAddress vpa = vpaRepository
                .findByUpiId(upiId)
                .filter(VirtualPaymentAddress::isActive)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Invalid or inactive UPI ID"));

        return walletRepository
                .findByUserId(vpa.getUser().getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Wallet not found for UPI ID"));
    }
}
