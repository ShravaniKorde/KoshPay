package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.entity.*;
import com.ewallet.wallet_service.repository.*;
import com.ewallet.wallet_service.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpiResolverServiceTest {
    private final VirtualPaymentAddressRepository vpaRepo = mock(VirtualPaymentAddressRepository.class);
    private final WalletRepository walletRepo = mock(WalletRepository.class);
    private final UpiResolverService service = new UpiResolverService(vpaRepo, walletRepo);

    @Test
    void testResolveSuccess() {
        User user = new User(); user.setId(1L);
        VirtualPaymentAddress vpa = new VirtualPaymentAddress();
        vpa.setUser(user); vpa.setActive(true);
        
        when(vpaRepo.findByUpiId("test@upi")).thenReturn(Optional.of(vpa));
        when(walletRepo.findByUserId(1L)).thenReturn(Optional.of(new Wallet()));
        
        assertNotNull(service.resolveToWallet("test@upi"));
    }

    @Test
    void testResolveNotFound() {
        when(vpaRepo.findByUpiId(any())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.resolveToWallet("wrong@upi"));
    }
}