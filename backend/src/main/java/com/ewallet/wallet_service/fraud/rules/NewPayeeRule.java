package com.ewallet.wallet_service.fraud.rules;

import com.ewallet.wallet_service.fraud.model.FraudContext;
import com.ewallet.wallet_service.fraud.service.FraudRule;
import com.ewallet.wallet_service.repository.TransactionRepository;

import org.springframework.stereotype.Component;

@Component
public class NewPayeeRule implements FraudRule {

    private final TransactionRepository transactionRepository;

    public NewPayeeRule(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public boolean isTriggered(FraudContext context) {

        boolean hasSentBefore =
            transactionRepository.existsByFromWalletIdAndToWalletId(
                context.getFromWalletId(),
                context.getToWalletId()
            );

        // Trigger ONLY if this is first time sending
        return !hasSentBefore;
    }

    @Override
    public int riskPoints() {
        return 40; // medium risk
    }
}