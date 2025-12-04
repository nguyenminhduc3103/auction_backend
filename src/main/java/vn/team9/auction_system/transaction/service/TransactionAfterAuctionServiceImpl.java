package vn.team9.auction_system.transaction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.team9.auction_system.common.dto.transaction.TransactionAfterAuctionResponse;
import vn.team9.auction_system.common.enums.TransactionStatus;
import vn.team9.auction_system.common.service.ITransactionAfterAuctionService;
import vn.team9.auction_system.transaction.mapper.TransactionMapper;
import vn.team9.auction_system.transaction.model.AccountTransaction;
import vn.team9.auction_system.transaction.model.TransactionAfterAuction;
import vn.team9.auction_system.transaction.repository.AccountTransactionRepository;
import vn.team9.auction_system.transaction.repository.TransactionAfterAuctionRepository;
import vn.team9.auction_system.user.model.User;
import vn.team9.auction_system.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionAfterAuctionServiceImpl implements ITransactionAfterAuctionService {

    private final TransactionAfterAuctionRepository transactionRepo;
    private final AccountTransactionRepository accountRepo;
    private final UserRepository userRepo;
    private final TransactionMapper transactionMapper;

    // ------------------------------------
    // Buyer thanh toán (tiền vào escrow)
    // ------------------------------------
    @Transactional
    public TransactionAfterAuctionResponse payTransaction(Long txnId, Long buyerId) {
        TransactionAfterAuction txn = transactionRepo.findById(txnId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        // Chỉ cho payer khi PENDING hoặc SHIPPED
        if (!(TransactionStatus.PENDING.name().equals(txn.getStatus())
                || TransactionStatus.SHIPPED.name().equals(txn.getStatus()))) {
            throw new IllegalStateException("Transaction is already processed or invalid");
        }

        // Lock buyer row để tránh race condition
        User buyer = userRepo.findByUserId(buyerId)
                .orElseThrow(() -> new EntityNotFoundException("Buyer not found"));
        User seller = txn.getSeller();

        BigDecimal amount = txn.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid transaction amount");
        }

        if (buyer.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance to initiate escrow payment");
        }

        // 1) Trừ tiền buyer 1 lần để hold (escrow)
        buyer.setBalance(buyer.getBalance().subtract(amount));
        userRepo.save(buyer);

        // 2) Tạo account transaction cho buyer và seller (PENDING)
        AccountTransaction buyerTxn = AccountTransaction.builder()
                .user(buyer)
                .amount(amount.negate())   // negative withdraw
                .type("TRANSFER")
                .status("PENDING")
                .build();
        accountRepo.save(buyerTxn);

        AccountTransaction sellerTxn = AccountTransaction.builder()
                .user(seller)
                .amount(amount)            // deposit pending
                .type("RECEIVED")
                .status("PENDING")
                .build();
        accountRepo.save(sellerTxn);

        // 3) Cập nhật trạng thái giao dịch: PAID (đang escrow)
        txn.setStatus(TransactionStatus.PAID.name());
        transactionRepo.save(txn);

        return toResponse(txn);
    }

    // ------------------------------------
    // Cập nhật trạng thái (SHIPPED, DONE, CANCELLED,...)
    // ------------------------------------
    @Override
    public TransactionAfterAuctionResponse updateTransactionStatus(Long txnId, String status) {
        TransactionAfterAuction txn = transactionRepo.findById(txnId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        String upperStatus = status == null ? null : status.toUpperCase();
        if (upperStatus == null) throw new IllegalArgumentException("Status is required");

        // Validate transition (ví dụ cơ bản)
        String current = txn.getStatus();
        if (TransactionStatus.DONE.name().equals(current) || TransactionStatus.CANCELLED.name().equals(current)) {
            throw new IllegalStateException("Cannot change status of a finished transaction");
        }

        txn.setStatus(upperStatus);
        transactionRepo.save(txn);

        if (TransactionStatus.DONE.name().equals(upperStatus)) {
            handleSuccessfulTransaction(txn);
        } else if (TransactionStatus.CANCELLED.name().equals(upperStatus)) {
            handleCancelledTransaction(txn);
        }

        return toResponse(txn);
    }

    // ------------------------------------
    // Huỷ transaction (PENDING | SHIPPED | PAID)
    // ------------------------------------
    @Override
    public TransactionAfterAuctionResponse cancelTransaction(Long txnId, String reason) {
        TransactionAfterAuction txn = transactionRepo.findById(txnId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        String currentStatus = txn.getStatus();
        if (!(TransactionStatus.PENDING.name().equals(currentStatus)
                || TransactionStatus.SHIPPED.name().equals(currentStatus)
                || TransactionStatus.PAID.name().equals(currentStatus))) {
            throw new IllegalStateException("Only pending, shipped or paid transactions can be cancelled");
        }

        txn.setStatus(TransactionStatus.CANCELLED.name());
        transactionRepo.save(txn);

        handleCancelledTransaction(txn);
        return toResponse(txn);
    }

    // ------------------------------------
    // Lấy transaction theo user
    // ------------------------------------
    @Override
    public List<TransactionAfterAuctionResponse> getTransactionsByUser(Long userId) {
        return transactionRepo.findByBuyer_UserIdOrSeller_UserId(userId, userId)
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    // ------------------------------------
    // Lấy transaction theo auction
    // ------------------------------------
    @Override
    public TransactionAfterAuctionResponse getTransactionByAuction(Long auctionId) {
        TransactionAfterAuction txn = transactionRepo.findByAuction_AuctionId(auctionId)
                .orElseThrow(() -> new EntityNotFoundException("No transaction found for this auction"));
        return toResponse(txn);
    }

    // ------------------------------------
    // Helper methods
    // ------------------------------------

    private void handleSuccessfulTransaction(TransactionAfterAuction txn) {
        User seller = txn.getSeller();
        BigDecimal amount = txn.getAmount();

        // 1) Cộng tiền cho seller (chỉ cộng 1 lần)
        seller.setBalance(seller.getBalance().add(amount));
        userRepo.save(seller);

        // 2) Update account transactions để từ PENDING -> SUCCESS
        // - buyer: type = WITHDRAW, status = PENDING => SUCCESS
        // - seller: type = DEPOSIT, status = PENDING => SUCCESS
        List<AccountTransaction> buyerPending = accountRepo.findByUserAndTypeAndStatus(txn.getBuyer(), "TRANSFER", "PENDING");
        buyerPending.forEach(t -> t.setStatus("SUCCESS"));
        accountRepo.saveAll(buyerPending);

        List<AccountTransaction> sellerPending = accountRepo.findByUserAndTypeAndStatus(seller, "RECEIVED", "PENDING");
        sellerPending.forEach(t -> t.setStatus("SUCCESS"));
        accountRepo.saveAll(sellerPending);
    }

    private void handleCancelledTransaction(TransactionAfterAuction txn) {
        User buyer = txn.getBuyer();
        User seller = txn.getSeller();
        BigDecimal amount = txn.getAmount();

        // 1) Hoàn lại balance cho buyer (vì đã trừ khi payTransaction)
        buyer.setBalance(buyer.getBalance().add(amount));
        userRepo.save(buyer);

        // 2) Cập nhật account transactions PENDING -> FAILED cho cả buyer & seller
        List<AccountTransaction> pendings = accountRepo.findByUsersAndStatus(List.of(buyer, seller), "PENDING");
        pendings.forEach(t -> t.setStatus("FAILED"));
        accountRepo.saveAll(pendings);
    }

    private TransactionAfterAuctionResponse toResponse(TransactionAfterAuction txn) {
        if (txn == null) return null;
        TransactionAfterAuctionResponse res = new TransactionAfterAuctionResponse();
        res.setId(txn.getTransactionId());
        res.setAuctionId(txn.getAuction() != null ? txn.getAuction().getAuctionId() : null);
        res.setBuyerId(txn.getBuyer() != null ? txn.getBuyer().getUserId() : null);
        res.setSellerId(txn.getSeller() != null ? txn.getSeller().getUserId() : null);
        res.setAmount(txn.getAmount());
        res.setStatus(txn.getStatus());
        res.setUpdatedAt(txn.getUpdatedAt());
        return res;
    }
}
