package com.ewallet.wallet_service.fraud.rules;

import com.ewallet.wallet_service.fraud.model.FraudContext;
import com.ewallet.wallet_service.fraud.service.FraudRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class HighAmountRule implements FraudRule {

    @Override
    public boolean isTriggered(FraudContext context) {
        return context.getAmount().compareTo(new BigDecimal("10000")) > 0;
    }

    @Override
    public int riskPoints() {
        return 70;
    }

    // @Override
    // public String ruleName() {
    //     return "HIGH_AMOUNT";
    // }
}