package com.ewallet.wallet_service.dto;

import com.ewallet.wallet_service.dto.request.*;
import com.ewallet.wallet_service.dto.response.*;
import com.ewallet.wallet_service.entity.TransactionStatus;
import com.ewallet.wallet_service.fraud.model.FraudResult;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    @Test
    void coverAllDtosAndModels() {
        FraudResult fr = new FraudResult(50, null); 
        assertNotNull(fr);

        ContactCreateRequest ccr = new ContactCreateRequest();
        ccr.setDisplayName("Test");
        ccr.setUpiId("test@upi");
        assertEquals("test@upi", ccr.getUpiId());

        LoginRequest lr = new LoginRequest();
        lr.setEmail("a@b.com");
        lr.setPassword("pass");
        lr.setAdminLogin(true);
        assertTrue(lr.isAdminLogin());

        SchedulePaymentRequest spr = new SchedulePaymentRequest();
        spr.setAmount(BigDecimal.TEN);
        spr.setReceiverUpiId("rec@upi");
        spr.setScheduledAt(Instant.now());
        assertNotNull(spr.getScheduledAt());

        TransferRequest tr = new TransferRequest();
        tr.setToWalletId(1L);
        tr.setAmount(BigDecimal.ONE);
        tr.setPin("1234");
        tr.setOtp("1111");
        assertEquals("1234", tr.getPin());

        UpiTransferRequest utr = new UpiTransferRequest();
        utr.setToUpiId("upi");
        utr.setAmount(BigDecimal.ONE);
        utr.setPin("1234");
        utr.setOtp("1111");
        assertNotNull(utr.getToUpiId());

        UserCreateRequest ucreq = new UserCreateRequest();
        ucreq.setName("User");
        ucreq.setInitialBalance(BigDecimal.valueOf(100));
        ucreq.setTransactionPin("9999");
        assertEquals("9999", ucreq.getTransactionPin());

        AdminAnalyticsResponse aar = new AdminAnalyticsResponse(1, 1, BigDecimal.ZERO, 95L, 0, 0, 1, BigDecimal.ZERO);
        assertNotNull(aar);

        AdminSummaryResponse asr = new AdminSummaryResponse(5, 10, BigDecimal.TEN, 0);
        assertEquals(5, asr.getTotalUsers());

        AdminTransactionResponse atr = new AdminTransactionResponse(1L, "a@upi", "b@upi", BigDecimal.ONE, "SUCCESS", Instant.now());
        assertNotNull(atr);

        AuthResponse auth = new AuthResponse("token-123");
        assertEquals("token-123", auth.getToken());

        BalanceUpdateResponse bur = new BalanceUpdateResponse(1L, BigDecimal.valueOf(500));
        assertEquals(1L, bur.getWalletId());

        ContactResponse cr = new ContactResponse(1L, "Name", "upi@id", LocalDateTime.now());
        assertEquals("Name", cr.getDisplayName());

        OtpResponse or = new OtpResponse("OK", "Sent", "123456");
        assertEquals("123456", or.getOtp());

        QrPayloadResponse qpr = new QrPayloadResponse("upi://pay");
        assertEquals("upi://pay", qpr.getPayload());

        ScheduledPaymentResponse spres = new ScheduledPaymentResponse(1L, "s@e.com", "r@u", BigDecimal.ONE, Instant.now(), TransactionStatus.SUCCESS, true);
        assertTrue(spres.isExecuted());

        TransactionResponse txr = new TransactionResponse(1L, "DEBIT", BigDecimal.ONE, 2L, Instant.now(), "SUCCESS", "a@u", "b@u");
        txr.setFromUpi("new@u");
        assertEquals("new@u", txr.getFromUpi());

        TransactionStatusDistributionResponse tsdr = new TransactionStatusDistributionResponse(1, 1, 1, 1);
        assertEquals(1, tsdr.getSuccess());

        UpiIdResponse uir = new UpiIdResponse("test@upi");
        assertEquals("test@upi", uir.getUpiId());

        WalletResponse wr = new WalletResponse(1L, BigDecimal.valueOf(1000));
        assertEquals(1L, wr.getWalletId());
    }
}