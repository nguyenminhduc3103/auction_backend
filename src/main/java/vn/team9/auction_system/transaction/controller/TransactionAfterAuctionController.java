package vn.team9.auction_system.transaction.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.team9.auction_system.common.dto.product.WonProductResponse;
import vn.team9.auction_system.common.dto.transaction.TransactionAfterAuctionRequest;
import vn.team9.auction_system.common.dto.transaction.TransactionAfterAuctionResponse;
import vn.team9.auction_system.common.service.ITransactionAfterAuctionService;
import vn.team9.auction_system.transaction.service.TransactionAfterAuctionServiceImpl;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/transactions/after-auction")
@RequiredArgsConstructor
public class TransactionAfterAuctionController {

    private final ITransactionAfterAuctionService transactionService;
    private final TransactionAfterAuctionServiceImpl transactionAfterAuctionService; // để dùng thêm hàm
                                                                                     // payTransaction()

    // ------------------------------------
    // Buyer thanh toán giao dịch
    // ------------------------------------
    @PostMapping("/{txnId}/pay")
    public ResponseEntity<TransactionAfterAuctionResponse> payTransaction(
            @PathVariable Long txnId,
            @RequestParam Long buyerId) {
        return ResponseEntity.ok(transactionAfterAuctionService.payTransaction(txnId, buyerId));
    }

    // ------------------------------------
    // Cập nhật trạng thái giao dịch (ví dụ: SHIPPED, DONE)
    // ------------------------------------
    @PutMapping("/{txnId}/status")
    public ResponseEntity<TransactionAfterAuctionResponse> updateStatus(
            @PathVariable Long txnId,
            @RequestParam String status) {
        return ResponseEntity.ok(transactionService.updateTransactionStatus(txnId, status));
    }

    // ------------------------------------
    // Huỷ transaction (chỉ khi PENDING)
    // ------------------------------------
    @PutMapping("/{txnId}/cancel")
    public ResponseEntity<TransactionAfterAuctionResponse> cancelTransaction(
            @PathVariable Long txnId,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(transactionService.cancelTransaction(txnId, reason));
    }

    // ------------------------------------
    // Lấy tất cả transaction của user
    // ------------------------------------
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionAfterAuctionResponse>> getTransactionsByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(transactionService.getTransactionsByUser(userId));
    }

    // ------------------------------------
    // Lấy tất cả transaction của seller
    // ------------------------------------
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<TransactionAfterAuctionResponse>> getTransactionsBySeller(
            @PathVariable Long sellerId) {
        return ResponseEntity.ok(transactionService.getTransactionsBySeller(sellerId));
    }

    // ------------------------------------
    // Lấy transaction theo auction
    // ------------------------------------
    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<TransactionAfterAuctionResponse> getTransactionByAuction(
            @PathVariable Long auctionId) {
        return ResponseEntity.ok(transactionService.getTransactionByAuction(auctionId));
    }


    //------------------------------------------
    // Lấy ra thông tin các sản phẩm đã thắng
    //------------------------------------------
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
