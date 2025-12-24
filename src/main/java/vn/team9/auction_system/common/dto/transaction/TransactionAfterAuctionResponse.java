package vn.team9.auction_system.common.dto.transaction;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionAfterAuctionResponse {
    private Long id;
    private Long auctionId;
    private Long buyerId;
    private Long sellerId;
    private BigDecimal amount;
    private String status; // PENDING, PAID, SHIPPED, DONE, CANCELLED
    private LocalDateTime updatedAt;

    // Product info (from auction)
    private Long productId;
    private String productName;
    private String productImageUrl;

    // Buyer info
    private String buyerName;
    private String buyerUsername;
}
