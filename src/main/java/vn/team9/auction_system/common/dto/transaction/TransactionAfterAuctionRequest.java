package vn.team9.auction_system.common.dto.transaction;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransactionAfterAuctionRequest {
    private Long auctionId;
    private Long buyerId;
    private Long sellerId;
    private BigDecimal amount;
}
