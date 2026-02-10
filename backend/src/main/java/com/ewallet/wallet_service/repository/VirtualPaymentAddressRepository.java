package com.ewallet.wallet_service.repository;

import com.ewallet.wallet_service.entity.VirtualPaymentAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VirtualPaymentAddressRepository
        extends JpaRepository<VirtualPaymentAddress, Long> {

    Optional<VirtualPaymentAddress> findByUpiId(String upiId);

    Optional<VirtualPaymentAddress> findByUserId(Long userId);

    boolean existsByUpiId(String upiId);
}
