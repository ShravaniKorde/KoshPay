package com.ewallet.wallet_service.fraud.rules;

import com.ewallet.wallet_service.fraud.model.FraudContext;
import com.ewallet.wallet_service.fraud.service.FraudRule;
import com.ewallet.wallet_service.repository.TransactionRepository;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class TransactionVelocityRule implements FraudRule {

    private static final int MAX_TXNS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(10);

    private final TransactionRepository transactionRepository;

    public TransactionVelocityRule(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public boolean isTriggered(FraudContext context) {

        Instant windowStart = Instant.now().minus(WINDOW);

        long recentTxCount =
            transactionRepository.countByFromWallet_IdAndTimestampAfter(
                context.getFromWalletId(),
                windowStart
            );

        // IMPORTANT: +1 because current txn is not yet persisted
        long effectiveCount = recentTxCount + 1;

        return effectiveCount > MAX_TXNS;
    }

    @Override
    public int riskPoints() {
        return 80;
    }
}