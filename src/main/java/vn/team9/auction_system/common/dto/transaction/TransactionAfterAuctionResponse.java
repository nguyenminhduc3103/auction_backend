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
}
