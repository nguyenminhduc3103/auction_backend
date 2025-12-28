package vn.team9.auction_system.transaction.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.team9.auction_system.common.dto.product.WonProductResponse;
import vn.team9.auction_system.common.dto.transaction.TransactionAfterAuctionResponse;
import vn.team9.auction_system.common.service.ITransactionAfterAuctionService;
import vn.team9.auction_system.transaction.service.TransactionAfterAuctionServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/api/transactions/after-auction")
@RequiredArgsConstructor
public class TransactionAfterAuctionController {

    private final ITransactionAfterAuctionService transactionService;
    private final TransactionAfterAuctionServiceImpl transactionAfterAuctionService; // for additional payTransaction() method

    // ------------------------------------
    // Buyer pays for transaction
    // ------------------------------------
    @PostMapping("/{txnId}/pay")
    @PreAuthorize("hasAuthority('POST:/api/transactions/after-auction/{txnId}/pay')")
    public ResponseEntity<TransactionAfterAuctionResponse> payTransaction(
            @PathVariable Long txnId,
            @RequestParam Long buyerId) {
        return ResponseEntity.ok(transactionAfterAuctionService.payTransaction(txnId, buyerId));
    }

    // ------------------------------------
    // Update transaction status (e.g. SHIPPED, DONE)
    // ------------------------------------
    @PutMapping("/{txnId}/status")
    @PreAuthorize("hasAuthority('PUT:/api/transactions/after-auction/{txnId}/status')")
    public ResponseEntity<TransactionAfterAuctionResponse> updateStatus(
            @PathVariable Long txnId,
            @RequestParam String status) {
        return ResponseEntity.ok(transactionService.updateTransactionStatus(txnId, status));
    }

    // ------------------------------------
    // Cancel transaction (only when PENDING)
    // ------------------------------------
    @PutMapping("/{txnId}/cancel")
    @PreAuthorize("hasAuthority('PUT:/api/transactions/after-auction/{txnId}/cancel')")
    public ResponseEntity<TransactionAfterAuctionResponse> cancelTransaction(
            @PathVariable Long txnId,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(transactionService.cancelTransaction(txnId, reason));
    }

    // ------------------------------------
    // Get all transactions of a user
    // ------------------------------------
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('GET:/api/transactions/after-auction/user/{userId}')")
    public ResponseEntity<List<TransactionAfterAuctionResponse>> getTransactionsByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(transactionService.getTransactionsByUser(userId));
    }

    // ------------------------------------
    // Get all transactions of a seller
    // ------------------------------------
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<TransactionAfterAuctionResponse>> getTransactionsBySeller(
            @PathVariable Long sellerId) {
        return ResponseEntity.ok(transactionService.getTransactionsBySeller(sellerId));
    }

    // ------------------------------------
    // Get transaction by auction
    // ------------------------------------
    @GetMapping("/auction/{auctionId}")
    @PreAuthorize("hasAuthority('GET:/api/transactions/after-auction/auction/{auctionId}')")
    public ResponseEntity<TransactionAfterAuctionResponse> getTransactionByAuction(
            @PathVariable Long auctionId) {
        return ResponseEntity.ok(transactionService.getTransactionByAuction(auctionId));
    }

    // ------------------------------------
    // Get information of won products
    // ------------------------------------
    @GetMapping("/{userId}/won-products")
    public ResponseEntity<List<WonProductResponse>> getWonProducts(
            @PathVariable Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long txnId
    ) {
        return ResponseEntity.ok(
                transactionService.getWonProducts(userId, status, txnId)
        );
    }
}