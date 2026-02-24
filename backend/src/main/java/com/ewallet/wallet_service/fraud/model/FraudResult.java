package com.ewallet.wallet_service.fraud.model;

import java.util.List;

import com.ewallet.wallet_service.fraud.service.FraudDecision;

public class FraudResult {

    private int riskScore;
    private FraudDecision decision;
    private List<String> triggeredRules;

    public FraudResult(int riskScore, FraudDecision decision, List<String> triggeredRules) {
    this.riskScore = riskScore;
    this.decision = decision;   
    this.triggeredRules = triggeredRules;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public FraudDecision getDecision() {
        return decision;
    }

    public void setDecision(FraudDecision decision) {
        this.decision = decision;
    }

    public List<String> getTriggeredRules() {
        return triggeredRules;
    }

    public void setTriggeredRules(List<String> triggeredRules) {
        this.triggeredRules = triggeredRules;
    }

}