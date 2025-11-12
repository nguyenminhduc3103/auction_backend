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
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!(txn.getStatus().equals(TransactionStatus.PENDING.name()) ||
                txn.getStatus().equals(TransactionStatus.SHIPPED.name()))) {
            throw new RuntimeException("Transaction is already processed or invalid");
        }

        User buyer = userRepo.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Buyer not found"));
        User seller = txn.getSeller();

        BigDecimal amount = txn.getAmount();
        if (buyer.getBalance().compareTo(amount) < 0)
            throw new RuntimeException("Insufficient balance to initiate escrow payment");

        buyer.setBalance(buyer.getBalance().subtract(amount));
        userRepo.save(buyer);
        // 1. Buyer tạo record rút tiền (chưa trừ thật)
        AccountTransaction buyerTxn = AccountTransaction.builder()
                .user(buyer)
                .amount(amount.negate())
                .type("WITHDRAW")
                .status("PENDING")
                .build();
        accountRepo.save(buyerTxn);

        // 2. Seller tạo record nhận tiền (chưa cộng thật)
        AccountTransaction sellerTxn = AccountTransaction.builder()
                .user(seller)
                .amount(amount)
                .type("DEPOSIT")
                .status("PENDING")
                .build();
        accountRepo.save(sellerTxn);

        // 3. TransactionAfterAuction sang trạng thái PAID (đang escrow)
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
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        String upperStatus = status.toUpperCase();
        txn.setStatus(upperStatus);
        transactionRepo.save(txn);

        // Nếu buyer xác nhận hoàn tất => chuyển tiền thật, hoàn tất giao dịch
        if (upperStatus.equals(TransactionStatus.DONE.name())) {
            handleSuccessfulTransaction(txn);
        }

        // Nếu giao dịch bị hủy => cập nhật escrow = FAILED, không thay đổi số dư
        if (upperStatus.equals(TransactionStatus.CANCELLED.name())) {
            handleCancelledTransaction(txn);
        }

        return toResponse(txn);
    }

    // ------------------------------------
    // Huỷ transaction (chỉ khi PENDING)
    // ------------------------------------
    @Override
    public TransactionAfterAuctionResponse cancelTransaction(Long txnId, String reason) {
        TransactionAfterAuction txn = transactionRepo.findById(txnId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        String currentStatus = txn.getStatus();
        if (!(currentStatus.equals(TransactionStatus.PENDING.name()) || currentStatus.equals(TransactionStatus.SHIPPED.name())|| currentStatus.equals(TransactionStatus.PAID.name()))) {
            throw new RuntimeException("Only pending or shipped or paid transactions can be cancelled");
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
                .orElseThrow(() -> new RuntimeException("No transaction found for this auction"));
        return toResponse(txn);
    }

    // ------------------------------------
    // Helper methods
    // ------------------------------------

    private void handleSuccessfulTransaction(TransactionAfterAuction txn) {
        User buyer = txn.getBuyer();
        User seller = txn.getSeller();
        BigDecimal amount = txn.getAmount();

        // Cập nhật số dư thật
        buyer.setBalance(buyer.getBalance().subtract(amount));
        seller.setBalance(seller.getBalance().add(amount));
        userRepo.save(buyer);
        userRepo.save(seller);

        // Update accounttransaction sang SUCCESS
        List<AccountTransaction> transactions = accountRepo.findAll();
        transactions.stream()
                .filter(t -> t.getUser().equals(buyer) && t.getType().equals("WITHDRAW") && t.getStatus().equals("PENDING"))
                .forEach(t -> t.setStatus("SUCCESS"));
        transactions.stream()
                .filter(t -> t.getUser().equals(seller) && t.getType().equals("DEPOSIT") && t.getStatus().equals("PENDING"))
                .forEach(t -> t.setStatus("SUCCESS"));
        accountRepo.saveAll(transactions);
    }

    private void handleCancelledTransaction(TransactionAfterAuction txn) {
        User buyer = txn.getBuyer();
        BigDecimal amount = txn.getAmount();

        // Hoàn lại balance cho buyer nếu trước đó đã trừ
        buyer.setBalance(buyer.getBalance().add(amount));
        userRepo.save(buyer);

        // Hủy: cập nhật tất cả transaction liên quan = FAILED
        List<AccountTransaction> transactions = accountRepo.findAll();
        transactions.stream()
                .filter(t -> t.getUser().equals(txn.getBuyer()) || t.getUser().equals(txn.getSeller()))
                .filter(t -> t.getStatus().equals("PENDING"))
                .forEach(t -> t.setStatus("FAILED"));
        accountRepo.saveAll(transactions);
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
