package com.ewallet.wallet_service.fraud.service;

import com.ewallet.wallet_service.fraud.model.FraudContext;

public interface FraudRule {

    boolean isTriggered(FraudContext context);

    int riskPoints();
}