package com.ewallet.wallet_service.fraud;

import com.ewallet.wallet_service.fraud.model.*;
import com.ewallet.wallet_service.fraud.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FraudDetectionServiceTest {

    private FraudDetectionService fraudDetectionService;
    private FraudRule rule1;
    private FraudRule rule2;

    @BeforeEach
    void setUp() {
        rule1 = mock(FraudRule.class);
        rule2 = mock(FraudRule.class);
        fraudDetectionService = new FraudDetectionService(List.of(rule1, rule2));
    }

    @Test
    void evaluate_ShouldBlock_WhenScoreIs70() {
        FraudContext context = new FraudContext(1L, 2L, new BigDecimal("1000"));
        when(rule1.isTriggered(any())).thenReturn(true);
        when(rule1.riskPoints()).thenReturn(40);
        when(rule2.isTriggered(any())).thenReturn(true);
        when(rule2.riskPoints()).thenReturn(30);

        FraudResult result = fraudDetectionService.evaluate(context);

        assertEquals(FraudDecision.BLOCK, result.getDecision());
        assertEquals(70, result.getRiskScore());
    }

    @Test
    void evaluate_ShouldAllow_WhenScoreIsBelow70() {
        FraudContext context = new FraudContext(1L, 2L, new BigDecimal("1000"));
        when(rule1.isTriggered(any())).thenReturn(true);
        when(rule1.riskPoints()).thenReturn(40);
        when(rule2.isTriggered(any())).thenReturn(false);

        FraudResult result = fraudDetectionService.evaluate(context);

        assertEquals(FraudDecision.ALLOW, result.getDecision());
        assertEquals(40, result.getRiskScore());
    }
}