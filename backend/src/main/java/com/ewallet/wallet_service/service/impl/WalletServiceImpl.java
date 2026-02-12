package com.ewallet.wallet_service.service.impl;

import com.ewallet.wallet_service.dto.response.TransactionResponse;
import com.ewallet.wallet_service.dto.response.WalletResponse;
import com.ewallet.wallet_service.entity.Transaction;
import com.ewallet.wallet_service.entity.User;
import com.ewallet.wallet_service.entity.Wallet;
import com.ewallet.wallet_service.exception.InsufficientBalanceException;
import com.ewallet.wallet_service.exception.ResourceNotFoundException;
import com.ewallet.wallet_service.repository.TransactionRepository;
import com.ewallet.wallet_service.repository.UserRepository;
import com.ewallet.wallet_service.repository.WalletRepository;
import com.ewallet.wallet_service.service.AuditLogService;
import com.ewallet.wallet_service.service.BalanceWebSocketService;
import com.ewallet.wallet_service.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

//fraud engine modules
import com.ewallet.wallet_service.fraud.model.FraudContext;
import com.ewallet.wallet_service.fraud.service.FraudDecision;
import com.ewallet.wallet_service.fraud.model.FraudResult;
import com.ewallet.wallet_service.fraud.service.FraudDetectionService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import com.ewallet.wallet_service.entity.TransactionStatus;
import com.ewallet.wallet_service.service.TransactionStatusService;
import com.ewallet.wallet_service.service.util.OtpService;
import com.ewallet.wallet_service.dto.response.OtpResponse;
import com.ewallet.wallet_service.exception.InvalidRequestException;

@Service
@Transactional
public class WalletServiceImpl implements WalletService {

    private static final Logger log =
            LoggerFactory.getLogger(WalletServiceImpl.class);

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final BalanceWebSocketService balanceWebSocketService;
    private final AuditLogService auditLogService;
    //fraud engine field
    private final FraudDetectionService fraudDetectionService;
    private final TransactionStatusService statusService; 
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder; 

    public WalletServiceImpl(
            WalletRepository walletRepository,
            TransactionRepository transactionRepository,
            UserRepository userRepository,
            BalanceWebSocketService balanceWebSocketService,
            AuditLogService auditLogService,
            FraudDetectionService fraudDetectionService,
            TransactionStatusService statusService,
            OtpService otpService,
            PasswordEncoder passwordEncoder 
    ) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.balanceWebSocketService = balanceWebSocketService;
        this.auditLogService = auditLogService;
        this.fraudDetectionService = fraudDetectionService;
        this.statusService = statusService;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder; 
    }

    // =============================
    // HELPER: CURRENT USER WALLET
    // =============================
    private Wallet getCurrentUserWallet() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Authenticated user not found: {}", email);
                    return new ResourceNotFoundException("User not found");
                });

        return walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> {
                    log.error("Wallet not found for userId={}", user.getId());
                    return new ResourceNotFoundException("Wallet not found");
                });
    }

    // =============================
    // GET MY BALANCE
    // =============================
    @Override
    @Transactional(readOnly = true)
    public WalletResponse getMyBalance() {

        Wallet wallet = getCurrentUserWallet();

        log.info("Balance fetched for userId={}, balance={}",
                wallet.getUser().getId(),
                wallet.getBalance());

        return new WalletResponse(wallet.getId(), wallet.getBalance());
    }

    // =============================
    // TRANSFER MONEY (ACID)
    // =============================
        @Override
        @Transactional
        public Object transfer(Long toWalletId, BigDecimal amount, String pin, String otp) {
                Wallet sender = getCurrentUserWallet();
                User user = sender.getUser();

        // 1. PIN VERIFICATION
        if (user.getTransactionPin() == null || !passwordEncoder.matches(pin, user.getTransactionPin())) {
                log.warn("Invalid PIN attempt for user: {}", user.getEmail());
                throw new IllegalArgumentException("Invalid Transaction PIN");
        }

        // 2. PREVENT SELF-TRANSFER ===
        if (sender.getId().equals(toWalletId)) {
                log.warn("Self-transfer attempt blocked for walletId: {}", sender.getId());
                throw new IllegalArgumentException("You cannot transfer money to your own wallet.");
        }
        Wallet receiver = walletRepository.findById(toWalletId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver wallet not found"));

        BigDecimal senderOldBal = sender.getBalance();
        
        // =============================
        // 3. FRAUD CHECK (BEFORE MONEY MOVE)
        // =============================
        FraudContext fraudContext = new FraudContext(
                sender.getId(),
                receiver.getId(),
                amount
        );
        fraudContext.setUserId(sender.getUser().getId());
        fraudContext.setTransactionTime(LocalDateTime.now());

        FraudResult fraudResult = fraudDetectionService.evaluate(fraudContext);

        if (fraudResult.getDecision() == FraudDecision.BLOCK) {

        log.warn(
                "Transfer blocked by fraud engine. fromWallet={}, toWallet={}, amount={}, riskScore={}",
                sender.getId(),
                receiver.getId(),
                amount,
                fraudResult.getRiskScore()
        );

        auditLogService.log(
                sender.getUser(),
                "TRANSFER",
                "FRAUD_BLOCK",
                senderOldBal,
                senderOldBal
        );
        throw new InvalidRequestException("Transaction blocked due to fraud risk");
        }

        // 4. OTP AUTHORIZATION (Challenge Gate)
        // Only ask for OTP if Risk > 50 OR amount is high
        if (fraudResult.getRiskScore() > 50 || amount.compareTo(new BigDecimal("1000")) > 0) {
            if (otp == null || otp.isEmpty()) {
                String generatedOtp = otpService.generateAndReturnOtp(user); 
                return new OtpResponse("OTP_REQUIRED", "Please verify using this code", generatedOtp);
            }

            if (!otpService.validateOtp(user, otp)) {
                throw new IllegalArgumentException("Invalid or expired OTP");
            }
        }

        // STEP 1: Create record in INITIATED status
        Transaction tx = new Transaction();
        tx.setFromWallet(sender);
        tx.setToWallet(receiver);
        tx.setAmount(amount);
        tx.setTimestamp(Instant.now());
        statusService.updateStatus(tx, TransactionStatus.INITIATED);
        
        log.info(">>> STATUS: INITIATED.");
        //     try { Thread.sleep(10000); } catch (InterruptedException e) { } // To check status in DB

        try {
            // HIGHLIGHT: 2. Validation
            if (sender.getBalance().compareTo(amount) < 0) {
                // This status persists even though we throw an exception next
                statusService.updateStatus(tx, TransactionStatus.FAILED); 
                throw new InsufficientBalanceException("Insufficient balance");
            }

           // HIGHLIGHT: 3. PENDING
            statusService.updateStatus(tx, TransactionStatus.PENDING);
            log.info(">>> STATUS: PENDING.");
            // try { Thread.sleep(10000); } catch (InterruptedException e) { } // To check status in DB

            // HIGHLIGHT: 4. Execution (Balance Update) // ACID section
            sender.setBalance(sender.getBalance().subtract(amount));
            receiver.setBalance(receiver.getBalance().add(amount));
            
            walletRepository.save(sender);
            walletRepository.save(receiver);

            // HIGHLIGHT: 5. SUCCESS
            statusService.updateStatus(tx, TransactionStatus.SUCCESS);
            log.info(">>> STATUS: SUCCESS.");

            // Notifications // AUDIT LOGS
            auditLogService.log(sender.getUser(), "TRANSFER", "SUCCESS", senderOldBal, sender.getBalance());
             
            // REAL-TIME UPDATES
            balanceWebSocketService.publishBalance(sender.getId(), sender.getBalance());
            balanceWebSocketService.publishBalance(receiver.getId(), receiver.getBalance());

            return "SUCCESS";

        } catch (Exception e) {
            // HIGHLIGHT: 6. FAILED (Catch-all for any errors)
            statusService.updateStatus(tx, TransactionStatus.FAILED);
            auditLogService.log(sender.getUser(), "TRANSFER", "FAILURE", senderOldBal, senderOldBal);
            throw e; // Rethrow to trigger rollback of balance changes
        }
    }

    // =============================
    // SINGLE PIN UPDATE LOGIC
    // =============================
    @Override
    public void updateTransactionPin(String newPin) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validation: Ensure it's numeric and 4 digits
        if (newPin == null || !newPin.matches("\\d{4}")) {
            throw new IllegalArgumentException("PIN must be exactly 4 numeric digits");
        }

        // Hash the PIN before saving
        user.setTransactionPin(passwordEncoder.encode(newPin));
        userRepository.save(user);
    
        log.info("Transaction PIN updated successfully for user: {}", email);
    }

     // =============================
     // TRANSACTION HISTORY
     // =============================
        @Override
        @Transactional(readOnly = true)
        public List<TransactionResponse> getMyTransactionHistory() {
                Wallet wallet = getCurrentUserWallet();
                List<Transaction> transactions = transactionRepository
                .findByFromWalletIdOrToWalletIdOrderByTimestampDesc(wallet.getId(), wallet.getId());

                return transactions.stream().map(tx -> {
                boolean isDebit = tx.getFromWallet().getId().equals(wallet.getId());
                return new TransactionResponse(
                    tx.getId(),
                    isDebit ? "DEBIT" : "CREDIT",
                    tx.getAmount(),
                    isDebit ? tx.getToWallet().getId() : tx.getFromWallet().getId(),
                    tx.getTimestamp(),
                    tx.getStatus().name() // HIGHLIGHT: Returns the Enum name
                );
                }).toList();
        }
}
