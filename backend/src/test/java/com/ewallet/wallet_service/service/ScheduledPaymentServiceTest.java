package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.entity.*;
import com.ewallet.wallet_service.repository.ScheduledPaymentRepository;
import com.ewallet.wallet_service.repository.UserRepository;
import com.ewallet.wallet_service.repository.VirtualPaymentAddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledPaymentServiceTest {

    @Mock private ScheduledPaymentRepository scheduledPaymentRepository;
    @Mock private UserRepository userRepository;
    @Mock private VirtualPaymentAddressRepository vpaRepository;

    @InjectMocks private ScheduledPaymentService scheduledPaymentService;

    private User testUser;
    private VirtualPaymentAddress testVpa;
    private ScheduledPayment testPayment;
    private final String email = "user@koshpay.com";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail(email);

        testVpa = new VirtualPaymentAddress();
        testVpa.setUpiId("receiver@upi");

        testPayment = new ScheduledPayment();
        testPayment.setId(1L);
        testPayment.setSender(testUser);
        testPayment.setReceiver(testVpa);
        testPayment.setAmount(new BigDecimal("100"));
        testPayment.setExecuted(false);
        testPayment.setStatus(TransactionStatus.PENDING);
    }

    @Test
    void createSchedule_Success() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(vpaRepository.findByUpiId("receiver@upi")).thenReturn(Optional.of(testVpa));
        when(scheduledPaymentRepository.save(any(ScheduledPayment.class))).thenAnswer(i -> i.getArguments()[0]);

        ScheduledPayment result = scheduledPaymentService.createSchedule(email, "receiver@upi", BigDecimal.TEN, Instant.now());

        assertNotNull(result);
        assertEquals(TransactionStatus.PENDING, result.getStatus());
        verify(scheduledPaymentRepository).save(any());
    }

    @Test
    void createSchedule_Throws_WhenUserNotFound() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> 
            scheduledPaymentService.createSchedule(email, "receiver@upi", BigDecimal.TEN, Instant.now()));
    }

    @Test
    void updateSchedule_Success() {
        when(scheduledPaymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(scheduledPaymentRepository.save(any())).thenReturn(testPayment);

        ScheduledPayment updated = scheduledPaymentService.updateSchedule(1L, email, new BigDecimal("500"), Instant.now());

        assertEquals(new BigDecimal("500"), updated.getAmount());
        verify(scheduledPaymentRepository).save(testPayment);
    }

    @Test
    void updateSchedule_Throws_WhenUnauthorized() {
        when(scheduledPaymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        assertThrows(RuntimeException.class, () -> 
            scheduledPaymentService.updateSchedule(1L, "wrong@email.com", BigDecimal.TEN, Instant.now()));
    }

    @Test
    void updateSchedule_Throws_WhenAlreadyExecuted() {
        testPayment.setExecuted(true);
        when(scheduledPaymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        
        assertThrows(RuntimeException.class, () -> 
            scheduledPaymentService.updateSchedule(1L, email, BigDecimal.TEN, Instant.now()));
    }

    @Test
    void cancelSchedule_Success() {
        when(scheduledPaymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));

        scheduledPaymentService.cancelSchedule(1L, email);

        assertEquals(TransactionStatus.FAILED, testPayment.getStatus());
        assertEquals("Cancelled by user", testPayment.getFailureReason());
        verify(scheduledPaymentRepository).save(testPayment);
    }

    @Test
    void cancelSchedule_Throws_WhenNotFound() {
        when(scheduledPaymentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> scheduledPaymentService.cancelSchedule(99L, email));
    }

    @Test
    void getUserScheduledPayments_Success() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(scheduledPaymentRepository.findBySender(testUser)).thenReturn(List.of(testPayment));

        List<ScheduledPayment> result = scheduledPaymentService.getUserScheduledPayments(email);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }
}