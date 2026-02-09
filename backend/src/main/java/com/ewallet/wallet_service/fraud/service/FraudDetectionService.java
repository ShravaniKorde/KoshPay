package com.ewallet.wallet_service.fraud.service;

import com.ewallet.wallet_service.fraud.model.FraudContext;
import com.ewallet.wallet_service.fraud.model.FraudResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FraudDetectionService {

    private final List<FraudRule> rules;

    public FraudDetectionService(List<FraudRule> rules) {
        this.rules = rules;
    }

    public FraudResult evaluate(FraudContext context) {

        int score = 0;

        for (FraudRule rule : rules) {
            if (rule.isTriggered(context)) {
                score += rule.riskPoints();
            }
        }

        FraudDecision decision = (score >= 70) ? FraudDecision.BLOCK : FraudDecision.ALLOW;

        return new FraudResult(score, decision);
    }
}