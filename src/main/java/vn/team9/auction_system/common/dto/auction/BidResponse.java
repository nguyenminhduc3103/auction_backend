package vn.team9.auction_system.common.dto.auction;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BidResponse {
    private Long id;
    private Long auctionId;
    private Long bidderId;
    private BigDecimal bidAmount;
    private BigDecimal maxAutoBidAmount;
    private BigDecimal stepAutoBidAmount;
    private LocalDateTime createdAt;
}
