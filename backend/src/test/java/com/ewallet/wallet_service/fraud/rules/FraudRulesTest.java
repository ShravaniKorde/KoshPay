package com.ewallet.wallet_service.fraud;

import com.ewallet.wallet_service.fraud.model.FraudContext;
import com.ewallet.wallet_service.fraud.rules.*;
import com.ewallet.wallet_service.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FraudRulesTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Test
    void testHighAmountRule() {
        HighAmountRule rule = new HighAmountRule();

        assertTrue(rule.isTriggered(new FraudContext(1L, 2L, new BigDecimal("10001"))));
        assertFalse(rule.isTriggered(new FraudContext(1L, 2L, new BigDecimal("5000"))));
        assertEquals(70, rule.riskPoints());
    }

    @Test
    void testWalletDrainPercentageRule() {
        WalletDrainPercentageRule rule = new WalletDrainPercentageRule();

        // 90% drain of balance 100 -> should trigger (threshold = 80%)
        FraudContext highDrain = new FraudContext(1L, 2L, new BigDecimal("90"));
        highDrain.setCurrentBalance(new BigDecimal("100"));
        assertTrue(rule.isTriggered(highDrain));

        // 10% drain -> should NOT trigger
        FraudContext lowDrain = new FraudContext(1L, 2L, new BigDecimal("10"));
        lowDrain.setCurrentBalance(new BigDecimal("100"));
        assertFalse(rule.isTriggered(lowDrain));

        // Null balance -> safe fail
        FraudContext nullBalance = new FraudContext(1L, 2L, new BigDecimal("50"));
        assertFalse(rule.isTriggered(nullBalance));

        assertEquals(40, rule.riskPoints());
    }

    @Test
    void testNewPayeeRule() {
        NewPayeeRule rule = new NewPayeeRule(transactionRepository);

        when(transactionRepository.existsByFromWalletIdAndToWalletId(1L, 2L))
                .thenReturn(false);
        assertTrue(rule.isTriggered(new FraudContext(1L, 2L, BigDecimal.TEN)));

        when(transactionRepository.existsByFromWalletIdAndToWalletId(1L, 3L))
                .thenReturn(true);
        assertFalse(rule.isTriggered(new FraudContext(1L, 3L, BigDecimal.TEN)));

        assertEquals(40, rule.riskPoints());
    }

    @Test
    void testTransactionVelocityRule() {
        TransactionVelocityRule rule = new TransactionVelocityRule(transactionRepository);

        FraudContext context = new FraudContext(1L, 2L, BigDecimal.TEN);
        context.setFromWalletId(1L);

        when(transactionRepository.countByFromWallet_IdAndTimestampAfter(eq(1L), any(Instant.class)))
                .thenReturn(5L);
        assertTrue(rule.isTriggered(context));

        when(transactionRepository.countByFromWallet_IdAndTimestampAfter(eq(1L), any(Instant.class)))
                .thenReturn(2L);
        assertFalse(rule.isTriggered(context));
        assertEquals(80, rule.riskPoints());
    }
}
