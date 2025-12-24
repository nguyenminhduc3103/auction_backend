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

    @Override
    @Transactional
    public UserWarningLogResponse createWarning(UserWarningLogRequest request) {
        User user = userRepo.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        TransactionAfterAuction txn = transactionRepo.findById(request.getTransactionId())
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        int previousViolations = warningRepo.countByUserAndType(user, request.getType());
        int currentViolationCount = previousViolations + 1;

        UserWarningLog log = new UserWarningLog();
        log.setUser(user);
        log.setTransaction(txn);
        log.setType(request.getType() != null ? request.getType() : "SHIPPED_NOT_PAID");
        log.setStatus(request.getStatus() != null ? request.getStatus() : "VIOLATION");
        log.setDescription(request.getDescription() != null ? request.getDescription() : "FOUND NG BEHAVIOR");
        log.setViolationCount(currentViolationCount);
        log.setCreatedAt(LocalDateTime.now());

        warningRepo.save(log);

        if (currentViolationCount >= 2) {
            user.setStatus("BANNED");
            user.setBannedUntil(null);
            user.setBanReason("Vi phạm " + currentViolationCount + " lần loại " + log.getType());
            userRepo.save(user);
        }

        UserWarningLogResponse response = new UserWarningLogResponse();
        response.setLogId(log.getLogId());
        response.setUserId(user.getUserId());
        response.setTransactionId(txn.getTransactionId());
        response.setType(log.getType());
        response.setStatus(log.getStatus());
        response.setDescription(log.getDescription());
        response.setViolationCount(currentViolationCount);
        response.setCreatedAt(log.getCreatedAt());

        return response;
    }
    @Override
    public List<UserWarningLogResponse> getAllWarnings() {
        return warningRepo.findAll().stream().map(log -> {
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
        }).toList();
    }   


    @Override
    public List<UserWarningLogResponse> getWarningsByUser(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return warningRepo.findByUser(user).stream().map(log -> {
            UserWarningLogResponse res = new UserWarningLogResponse();
            res.setLogId(log.getLogId());
            res.setUserId(user.getUserId());
            res.setTransactionId(log.getTransaction().getTransactionId());
            res.setType(log.getType());
            res.setStatus(log.getStatus());
            res.setDescription(log.getDescription());
            res.setViolationCount(log.getViolationCount());
            res.setCreatedAt(log.getCreatedAt());
            return res;
        }).toList();
    }

    @Override
    public List<UserWarningLogResponse> getWarningsByTransaction(Long transactionId) {
        TransactionAfterAuction txn = transactionRepo.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        return warningRepo.findByTransaction_TransactionId(txn.getTransactionId())
                .stream()
                .map(log -> {
                    UserWarningLogResponse res = new UserWarningLogResponse();
                    res.setLogId(log.getLogId());
                    res.setUserId(log.getUser().getUserId());
                    res.setTransactionId(txn.getTransactionId());
                    res.setType(log.getType());
                    res.setStatus(log.getStatus());
                    res.setDescription(log.getDescription());
                    res.setViolationCount(log.getViolationCount());
                    res.setCreatedAt(log.getCreatedAt());
                    return res;
                }).toList();
    }

    /**
     * Tự động tạo cảnh báo cho các giao dịch SHIPPED > 36h
     */
    @Transactional
    public void warnPendingTransactionsOver36h() {
        NotiViolation();
    }

    /**
     * Chạy ngay khi server khởi động xong
     */
    @EventListener(ApplicationReadyEvent.class)
    public void runOnStartup() {
        warnPendingTransactionsOver36h();
    }

    /**
     * Scheduler tự động tạo cảnh báo cho buyer chưa thanh toán sau 36h
     * Chạy mỗi ngày lúc 00:00
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void autoWarnPendingTransactionsDaily() {
        NotiViolation();
    }

    private void NotiViolation() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(36);

        List<TransactionAfterAuction> overdueTxns = transactionRepo
                .findByStatusAndUpdatedAtBefore("SHIPPED", threshold);

        for (TransactionAfterAuction txn : overdueTxns) {
            User user = txn.getBuyer();

            txn.setStatus("CANCELLED");
            transactionRepo.save(txn);

            int previousViolations = warningRepo.countByUserAndType(user, "SHIPPED_NOT_PAID");
            int currentViolationCount = previousViolations + 1;

            UserWarningLog log = new UserWarningLog();
            log.setUser(user);
            log.setTransaction(txn);
            log.setType("SHIPPED_NOT_PAID");
            log.setStatus("VIOLATION");
            log.setDescription("Buyer chưa thanh toán sau 36h");
            log.setViolationCount(currentViolationCount);
            log.setCreatedAt(LocalDateTime.now());

            warningRepo.save(log);

            if (currentViolationCount >= 2) {
                user.setStatus("BANNED");
                user.setBannedUntil(null);
                user.setBanReason("Vi phạm " + currentViolationCount + " lần: chưa thanh toán sau 36h");
                userRepo.save(user);
            }
        }
    }

}

