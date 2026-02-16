package com.ewallet.wallet_service.repository;

import com.ewallet.wallet_service.entity.Transaction;
import com.ewallet.wallet_service.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // =====================================================
    // USER TRANSACTION HISTORY
    // =====================================================
    List<Transaction> findByFromWalletIdOrToWalletIdOrderByTimestampDesc(
            Long fromWalletId,
            Long toWalletId
    );

    // =====================================================
    // FRAUD ENGINE SUPPORT
    // =====================================================
    long countByFromWallet_IdAndTimestampAfter(
            Long fromWalletId,
            Instant timestamp
    );

    boolean existsByFromWalletIdAndToWalletId(
            Long fromWalletId,
            Long toWalletId
    );

    // =====================================================
    // ADMIN ANALYTICS
    // =====================================================

    // Total transactions by status
    long countByStatus(TransactionStatus status);

    // Total successful transaction volume
    @Query("""
           SELECT COALESCE(SUM(t.amount), 0)
           FROM Transaction t
           WHERE t.status = com.ewallet.wallet_service.entity.TransactionStatus.SUCCESS
           """)
    BigDecimal sumSuccessfulTransactionVolume();

    // Total volume of ALL transactions (regardless of status)
    @Query("""
           SELECT COALESCE(SUM(t.amount), 0)
           FROM Transaction t
           """)
    BigDecimal sumTotalVolume();

    // Count today's transactions
    @Query("""
           SELECT COUNT(t)
           FROM Transaction t
           WHERE t.timestamp >= :start
           """)
    long countTransactionsAfter(@Param("start") Instant start);

    // Sum today's transaction volume
    @Query("""
           SELECT COALESCE(SUM(t.amount), 0)
           FROM Transaction t
           WHERE t.timestamp >= :start
           """)
    BigDecimal sumVolumeAfter(@Param("start") Instant start);

}
