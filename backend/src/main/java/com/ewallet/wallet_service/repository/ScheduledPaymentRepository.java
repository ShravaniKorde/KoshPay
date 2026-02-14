package com.ewallet.wallet_service.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ewallet.wallet_service.entity.ScheduledPayment;
import com.ewallet.wallet_service.entity.TransactionStatus;
import com.ewallet.wallet_service.entity.User;

public interface ScheduledPaymentRepository
        extends JpaRepository<ScheduledPayment, Long> {

    @Query("""
    SELECT sp FROM ScheduledPayment sp
    JOIN FETCH sp.sender
    JOIN FETCH sp.receiver
    WHERE sp.executed = false
    AND sp.scheduledAt <= :time
    """)
    List<ScheduledPayment> findPendingPayments(@Param("time") Instant time);

    // Fetch user’s scheduled payments
    List<ScheduledPayment> 
        findBySender(User sender);

    // Fetch pending payments of a user
    List<ScheduledPayment> 
        findBySenderAndStatus(User sender, TransactionStatus status);
}