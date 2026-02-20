package com.ewallet.wallet_service.fraud.rules;

import com.ewallet.wallet_service.fraud.model.FraudContext;
import com.ewallet.wallet_service.fraud.service.FraudRule;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

@Component
public class WalletDrainPercentageRule implements FraudRule {

    private static final BigDecimal THRESHOLD_PERCENT =
            new BigDecimal("0.80"); // 75%

    @Override
    public boolean isTriggered(FraudContext context) {

        BigDecimal balance = context.getCurrentBalance();
        BigDecimal amount = context.getAmount();

        // Safety check
        if (balance == null || balance.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        BigDecimal drainRatio =
                amount.divide(balance, 4, RoundingMode.HALF_UP);

        return drainRatio.compareTo(THRESHOLD_PERCENT) >= 0;
    }

    @Override
    public int riskPoints() {
        return 40; // medium-high risk
    }
}