package vn.team9.auction_system.common.dto.auction;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BidRequest {
    private Long auctionId;
    private Long bidderId;
    private BigDecimal bidAmount;
    private BigDecimal maxAutoBidAmount;
    private BigDecimal stepAutoBidAmount;
}
