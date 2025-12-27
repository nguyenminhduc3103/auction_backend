package vn.team9.auction_system.transaction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.team9.auction_system.auction.model.Auction;
import vn.team9.auction_system.common.dto.product.WonProductResponse;
import vn.team9.auction_system.common.dto.transaction.TransactionAfterAuctionResponse;
import vn.team9.auction_system.common.enums.TransactionStatus;
import vn.team9.auction_system.common.service.ITransactionAfterAuctionService;
import vn.team9.auction_system.product.model.Product;
import vn.team9.auction_system.transaction.mapper.TransactionMapper;
import vn.team9.auction_system.transaction.model.AccountTransaction;
import vn.team9.auction_system.transaction.model.TransactionAfterAuction;
import vn.team9.auction_system.transaction.repository.AccountTransactionRepository;
import vn.team9.auction_system.transaction.repository.TransactionAfterAuctionRepository;
import vn.team9.auction_system.user.model.User;
import vn.team9.auction_system.user.repository.UserRepository;
import vn.team9.auction_system.feedback.event.NotificationEventPublisher;
import lombok.extern.slf4j.Slf4j;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TransactionAfterAuctionServiceImpl implements ITransactionAfterAuctionService {

    private final TransactionAfterAuctionRepository transactionRepo;
    private final AccountTransactionRepository accountRepo;
    private final UserRepository userRepo;
    private final TransactionMapper transactionMapper;
    private final AccountTransactionServiceImpl accountTransactionServiceImpl;
    private final NotificationEventPublisher notificationPublisher;

    // ------------------------------------
    // Create transaction after auction (called when auction closes)
    // ------------------------------------
    public TransactionAfterAuctionResponse createTransactionAfterAuction(Auction auction, User buyer, User seller, BigDecimal amount) {
        TransactionAfterAuction txn = new TransactionAfterAuction();
        txn.setAuction(auction);
        txn.setBuyer(buyer);
        txn.setSeller(seller);
        txn.setAmount(amount);
        txn.setStatus(TransactionStatus.PENDING.name());
        transactionRepo.save(txn);

        // 🆕 Send PAYMENT_DUE notification to buyer
        try {
            notificationPublisher.publishPaymentDueNotification(
                buyer.getUserId(),
                amount.doubleValue(),
                txn.getTransactionId()
            );
            log.info("✅ PAYMENT_DUE notification sent to buyer");
        } catch (Exception e) {
            log.warn("⚠️ Failed to send payment due notification: {}", e.getMessage());
        }

        // 🆕 Send PAYMENT_PENDING notification to seller
        try {
            notificationPublisher.publishPaymentPendingNotification(
                seller.getUserId(),
                buyer.getFullName(),
                amount.doubleValue(),
                txn.getTransactionId()
            );
            log.info("✅ PAYMENT_PENDING notification sent to seller");
        } catch (Exception e) {
            log.warn("⚠️ Failed to send payment pending notification: {}", e.getMessage());
        }

        return toResponse(txn);
    }

    // ------------------------------------
    // Buyer pays (money goes into escrow)
    // ------------------------------------
    @Transactional
    public TransactionAfterAuctionResponse payTransaction(Long txnId, Long buyerId) {
        TransactionAfterAuction txn = transactionRepo.findById(txnId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        // Only allow payment when PENDING or SHIPPED
        if (!(TransactionStatus.PENDING.name().equals(txn.getStatus())
                || TransactionStatus.SHIPPED.name().equals(txn.getStatus()))) {
            throw new IllegalStateException("Transaction is already processed or invalid");
        }

        // Lock buyer row to avoid race condition
        User buyer = userRepo.findByUserId(buyerId)
                .orElseThrow(() -> new EntityNotFoundException("Buyer not found"));
        User seller = txn.getSeller();

        BigDecimal amount = txn.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid transaction amount");
        }

        BigDecimal withdrawable = accountTransactionServiceImpl.getWithdrawable(buyer.getUserId());
        if (withdrawable.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient available balance to initiate escrow payment");
        }

        // 1) Deduct buyer's balance once to hold (escrow)
        buyer.setBalance(buyer.getBalance().subtract(amount));
        userRepo.save(buyer);

        // 2) Create account transaction for buyer and seller (PENDING)
        AccountTransaction buyerTxn = AccountTransaction.builder()
                .user(buyer)
                .amount(amount.negate()) // negative withdrawal
                .type("TRANSFER")
                .status("PENDING")
                .build();
        accountRepo.save(buyerTxn);

        AccountTransaction sellerTxn = AccountTransaction.builder()
                .user(seller)
                .amount(amount) // deposit pending
                .type("RECEIVED")
                .status("PENDING")
                .build();
        accountRepo.save(sellerTxn);

        // 3) Update transaction status: PAID (in escrow)
        txn.setStatus(TransactionStatus.PAID.name());
        transactionRepo.save(txn);

        // 4️⃣ Send PAYMENT_SUCCESS notification to buyer
        try {
            notificationPublisher.publishPaymentSuccessNotification(
                buyer.getUserId(),
                amount.doubleValue(),
                txn.getTransactionId()
            );
            log.info("✅ PAYMENT_SUCCESS notification sent to buyer");
        } catch (Exception e) {
            log.warn("⚠️ Failed to send payment success notification: {}", e.getMessage());
        }

        // 5️⃣ Send PAYMENT_CONFIRMED notification to seller
        try {
            notificationPublisher.publishPaymentConfirmedNotification(
                seller.getUserId(),
                buyer.getFullName(),
                amount.doubleValue(),
                txn.getTransactionId()
            );
            log.info("✅ PAYMENT_CONFIRMED notification sent to seller");
        } catch (Exception e) {
            log.warn("⚠️ Failed to send payment confirmed notification: {}", e.getMessage());
        }

        return toResponse(txn);
    }

    // ------------------------------------
    // Update status (SHIPPED, DONE, CANCELLED,...)
    // ------------------------------------
    @Override
    public TransactionAfterAuctionResponse updateTransactionStatus(Long txnId, String status) {
        TransactionAfterAuction txn = transactionRepo.findById(txnId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        String upperStatus = status == null ? null : status.toUpperCase();
        if (upperStatus == null)
            throw new IllegalArgumentException("Status is required");

        // Validate transition (basic example)
        String current = txn.getStatus();
        if (TransactionStatus.DONE.name().equals(current) || TransactionStatus.CANCELLED.name().equals(current)) {
            throw new IllegalStateException("Cannot change status of a finished transaction");
        }

        txn.setStatus(upperStatus);
        transactionRepo.save(txn);

        // If changed to DONE then update account transactions
        if (TransactionStatus.DONE.name().equals(upperStatus)) {
            handleSuccessfulTransaction(txn);
            // If changed to CANCELLED handle differently
        } else if (TransactionStatus.CANCELLED.name().equals(upperStatus)) {
            handleCancelledTransaction(txn);
        }

        return toResponse(txn);
    }

    // ------------------------------------
    // Cancel transaction (PENDING | SHIPPED | PAID)
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
    // Get transactions by user
    // ------------------------------------
    @Override
    public List<TransactionAfterAuctionResponse> getTransactionsByUser(Long userId) {
        return transactionRepo.findByBuyer_UserIdOrSeller_UserId(userId, userId)
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    // ------------------------------------
    // Get transactions by seller
    // ------------------------------------
    @Override
    public List<TransactionAfterAuctionResponse> getTransactionsBySeller(Long sellerId) {
        return transactionRepo.findBySeller_UserId(sellerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ------------------------------------
    // Get transaction by auction
    // ------------------------------------
    @Override
    public TransactionAfterAuctionResponse getTransactionByAuction(Long auctionId) {
        TransactionAfterAuction txn = transactionRepo.findByAuction_AuctionId(auctionId)
                .orElseThrow(() -> new EntityNotFoundException("No transaction found for this auction"));
        return toResponse(txn);
    }

    @Override
    public List<WonProductResponse> getWonProducts(Long userId, String status, Long txnId) {

        // =========================
        // CASE 1: Has txnId → get single transaction
        // =========================
        if (txnId != null) {
            TransactionAfterAuction t = transactionRepo
                    .findByTransactionIdAndBuyerUserId(txnId, userId)
                    .orElseThrow(() ->
                            new EntityNotFoundException("Transaction does not exist")
                    );

            return List.of(mapToWonProductResponse(t));
        }

        // =========================
        // CASE 2: Get list by status
        // =========================
        String filterStatus =
                (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status))
                        ? null
                        : status;

        List<TransactionAfterAuction> transactions =
                transactionRepo.findWonAuctions(userId, filterStatus);

        return transactions.stream()
                .map(this::mapToWonProductResponse)
                .toList();
    }

    // ------------------------------------
    // Helper methods
    // ------------------------------------

    private void handleSuccessfulTransaction(TransactionAfterAuction txn) {
        User seller = txn.getSeller();
        User buyer = txn.getBuyer();
        BigDecimal amount = txn.getAmount();

        // 1) Add money to seller (only once)
        seller.setBalance(seller.getBalance().add(amount));
        userRepo.save(seller);

        // 2) Update account transactions from PENDING -> SUCCESS
        // - buyer: type = WITHDRAW, status = PENDING => SUCCESS
        // - seller: type = DEPOSIT, status = PENDING => SUCCESS
        List<AccountTransaction> buyerPending = accountRepo.findByUserAndTypeAndStatus(buyer, "TRANSFER",
                "PENDING");
        buyerPending.forEach(t -> t.setStatus("SUCCESS"));
        accountRepo.saveAll(buyerPending);

        List<AccountTransaction> sellerPending = accountRepo.findByUserAndTypeAndStatus(seller, "RECEIVED", "PENDING");
        sellerPending.forEach(t -> t.setStatus("SUCCESS"));
        accountRepo.saveAll(sellerPending);

        // 3️⃣ Send SHIPMENT_CONFIRMED notification to buyer
        try {
            if (txn.getAuction() != null && txn.getAuction().getProduct() != null) {
                String productName = txn.getAuction().getProduct().getName();
                String trackingNumber = "TXN-" + txn.getTransactionId(); // Generate tracking number
                notificationPublisher.publishShipmentConfirmedNotification(
                    buyer.getUserId(),
                    productName,
                    trackingNumber
                );
                log.info("✅ SHIPMENT_CONFIRMED notification sent to buyer: {} for product: {}", 
                    buyer.getUserId(), productName);
            }
        } catch (Exception e) {
            log.warn("⚠️ Failed to send shipment notification: {}", e.getMessage());
        }
    }

    private void handleCancelledTransaction(TransactionAfterAuction txn) {
        User buyer = txn.getBuyer();
        User seller = txn.getSeller();
        BigDecimal amount = txn.getAmount();

        // 1) Refund balance to buyer (was deducted during payTransaction)
        buyer.setBalance(buyer.getBalance().add(amount));
        userRepo.save(buyer);

        // 2) Update account transactions PENDING -> FAILED for both buyer & seller
        List<AccountTransaction> pendings = accountRepo.findByUsersAndStatus(List.of(buyer, seller), "PENDING");
        pendings.forEach(t -> t.setStatus("FAILED"));
        accountRepo.saveAll(pendings);

        // 3️⃣ Send PAYMENT_FAILED notification to buyer
        try {
            notificationPublisher.publishPaymentFailedNotification(
                buyer.getUserId(),
                amount.doubleValue(),
                "Transaction cancelled",
                txn.getTransactionId()
            );
            log.info("✅ PAYMENT_FAILED notification sent to buyer");
        } catch (Exception e) {
            log.warn("⚠️ Failed to send payment failed notification: {}", e.getMessage());
        }
    }

    private TransactionAfterAuctionResponse toResponse(TransactionAfterAuction txn) {
        if (txn == null)
            return null;
        TransactionAfterAuctionResponse res = new TransactionAfterAuctionResponse();
        res.setId(txn.getTransactionId());
        res.setAuctionId(txn.getAuction() != null ? txn.getAuction().getAuctionId() : null);
        res.setBuyerId(txn.getBuyer() != null ? txn.getBuyer().getUserId() : null);
        res.setSellerId(txn.getSeller() != null ? txn.getSeller().getUserId() : null);
        res.setAmount(txn.getAmount());
        res.setStatus(txn.getStatus());
        res.setUpdatedAt(txn.getUpdatedAt());

        // Product info (from auction → product)
        if (txn.getAuction() != null && txn.getAuction().getProduct() != null) {
            var product = txn.getAuction().getProduct();
            res.setProductId(product.getProductId());
            res.setProductName(product.getName());
            // Get thumbnail image
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                var thumbnailOpt = product.getImages().stream()
                        .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
                        .findFirst();
                if (thumbnailOpt.isPresent()) {
                    res.setProductImageUrl(thumbnailOpt.get().getUrl());
                } else {
                    res.setProductImageUrl(product.getImages().getFirst().getUrl());
                }
            }
        }

        // Buyer info
        if (txn.getBuyer() != null) {
            res.setBuyerName(txn.getBuyer().getFullName());
            res.setBuyerUsername(txn.getBuyer().getUsername());
        }

        return res;
    }

    private WonProductResponse mapToWonProductResponse(TransactionAfterAuction t) {
        Auction a = t.getAuction();
        Product p = a.getProduct();

        WonProductResponse res = new WonProductResponse();
        res.setTransactionId(t.getTransactionId());
        res.setTransactionStatus(t.getStatus());
        res.setAmount(t.getAmount());
        res.setUpdatedAt(t.getUpdatedAt());

        res.setAuctionId(a.getAuctionId());
        res.setAuctionStatus(a.getStatus());

        res.setProductId(p.getProductId());
        res.setProductName(p.getName());
        res.setProductImage(p.getImageUrl());
        res.setProductCategory(p.getCategory());
        res.setProductDescription(p.getDescription());
        res.setProductPrice(p.getEstimatePrice());

        res.setSellerId(p.getSeller().getUserId());
        res.setSellerName(p.getSeller().getFullName());

        return res;
    }
}