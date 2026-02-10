package com.ewallet.wallet_service.repository;

import com.ewallet.wallet_service.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByFromWalletIdOrToWalletIdOrderByTimestampDesc(
            Long fromWalletId,
            Long toWalletId
    );

    //fraud engine
    long countByFromWallet_IdAndTimestampAfter(
            Long fromWalletId,
            Instant timestamp
    );

    boolean existsByFromWalletIdAndToWalletId(
        Long fromWalletId,
        Long toWalletId
    );

}
