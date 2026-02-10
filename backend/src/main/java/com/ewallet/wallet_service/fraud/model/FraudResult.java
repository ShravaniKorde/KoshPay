package com.ewallet.wallet_service.fraud.model;

import com.ewallet.wallet_service.fraud.service.FraudDecision;

public class FraudResult {

    private int riskScore;
    private FraudDecision decision;

    public FraudResult(int riskScore, FraudDecision decision) {
    this.riskScore = riskScore;
    this.decision = decision;   
    }

    public int getRiskScore() {
        return riskScore;
    }

    public FraudDecision getDecision() {
        return decision;
    }

}