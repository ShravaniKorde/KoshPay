package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.entity.Transaction;
import com.ewallet.wallet_service.entity.TransactionStatus;
import com.ewallet.wallet_service.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionStatusService {

    private final TransactionRepository transactionRepository;

    public TransactionStatusService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * REQUIRES_NEW starts a brand new database transaction.
     * Even if the main transfer fails and rolls back, 
     * this status update will be COMMITTED to the database.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(Transaction tx, TransactionStatus status) {
        tx.setStatus(status);
        transactionRepository.saveAndFlush(tx);
    }
}