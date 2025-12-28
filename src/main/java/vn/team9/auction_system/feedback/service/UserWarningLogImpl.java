package vn.team9.auction_system.feedback.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.team9.auction_system.common.dto.admin.UserWarningLogRequest;
import vn.team9.auction_system.common.dto.admin.UserWarningLogResponse;
import vn.team9.auction_system.common.service.IUserWarningService;
import vn.team9.auction_system.feedback.model.UserWarningLog;
import vn.team9.auction_system.feedback.repository.UserWarningLogRepository;
import vn.team9.auction_system.transaction.model.TransactionAfterAuction;
import vn.team9.auction_system.transaction.repository.TransactionAfterAuctionRepository;
import vn.team9.auction_system.user.model.User;
import vn.team9.auction_system.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserWarningLogImpl implements IUserWarningService {

    private final UserWarningLogRepository warningRepo;
    private final UserRepository userRepo;
    private final TransactionAfterAuctionRepository transactionRepo;

    // PUBLIC API
    @Override
    @Transactional
    public UserWarningLogResponse createWarning(UserWarningLogRequest request) {

        User user = getUserOrThrow(request.getUserId());
        TransactionAfterAuction txn = getTransactionOrThrow(request.getTransactionId());

        UserWarningLog log = handleViolation(
                user,
                txn,
                request.getType() != null ? request.getType() : "SHIPPED_NOT_PAID",
                request.getDescription() != null ? request.getDescription() : "FOUND SUSPICIOUS BEHAVIOR"
        );

        return mapToResponse(log);
    }

    @Override
    public List<UserWarningLogResponse> getAllWarnings() {
        return warningRepo.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<UserWarningLogResponse> getWarningsByUser(Long userId) {
        User user = getUserOrThrow(userId);
        return warningRepo.findByUser(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<UserWarningLogResponse> getWarningsByTransaction(Long transactionId) {
        TransactionAfterAuction txn = getTransactionOrThrow(transactionId);
        return warningRepo.findByTransaction_TransactionId(txn.getTransactionId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // AUTO WARNING (SCHEDULER)
    /**
     * Run immediately after server startup
     */
    @EventListener(ApplicationReadyEvent.class)
    public void runOnStartup() {
        processOverdueTransactions();
    }

    /**
     * Auto run daily at 00:00
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void autoWarnPendingTransactionsDaily() {
        processOverdueTransactions();
    }

    // CORE BUSINESS LOGIC
    @Transactional
    public void processOverdueTransactions() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(36);

        List<TransactionAfterAuction> overdueTxns =
                transactionRepo.findByStatusAndUpdatedAtBefore("SHIPPED", threshold);

        for (TransactionAfterAuction txn : overdueTxns) {
            User user = txn.getBuyer();

            txn.setStatus("CANCELLED");
            transactionRepo.save(txn);

            handleViolation(
                    user,
                    txn,
                    "SHIPPED_NOT_PAID",
                    "Buyer hasn't paid after 36h"
            );
        }
    }

    /**
     * Centralized violation handling
     */
    private UserWarningLog handleViolation(
            User user,
            TransactionAfterAuction txn,
            String type,
            String description
    ) {
        int previousViolations = warningRepo.countByUserAndType(user, type);
        int currentViolationCount = previousViolations + 1;

        UserWarningLog log = new UserWarningLog();
        log.setUser(user);
        log.setTransaction(txn);
        log.setType(type);
        log.setStatus("VIOLATION");
        log.setDescription(description);
        log.setViolationCount(currentViolationCount);
        log.setCreatedAt(LocalDateTime.now());

        warningRepo.save(log);

        if (currentViolationCount >= 2) {
            banUser(user, type, currentViolationCount);
        }

        return log;
    }

    private void banUser(User user, String type, int count) {
        user.setStatus("BANNED");
        user.setBannedUntil(null);
        user.setBanReason("Violated " + count + " times for type " + type);
        userRepo.save(user);
    }

    private UserWarningLogResponse mapToResponse(UserWarningLog log) {
        UserWarningLogResponse res = new UserWarningLogResponse();
        res.setLogId(log.getLogId());
        res.setUserId(log.getUser().getUserId());
        res.setTransactionId(log.getTransaction().getTransactionId());
        res.setType(log.getType());
        res.setStatus(log.getStatus());
        res.setDescription(log.getDescription());
        res.setViolationCount(log.getViolationCount());
        res.setCreatedAt(log.getCreatedAt());
        return res;
    }

    private User getUserOrThrow(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private TransactionAfterAuction getTransactionOrThrow(Long txnId) {
        return transactionRepo.findById(txnId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }
}
