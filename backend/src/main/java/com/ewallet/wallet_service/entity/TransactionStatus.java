package com.ewallet.wallet_service.entity;

public enum TransactionStatus {
    INITIATED, // Created in DB, but no money moved yet
    PENDING,   // Waiting for external checks (UPI/PIN/Fraud)
    SUCCESS,   // Money successfully moved
    FAILED     // Aborted (Insufficient funds/Error)
}
