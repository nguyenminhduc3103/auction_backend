package vn.team9.auction_system.common.dto.auction;

import vn.team9.auction_system.common.base.BaseResponse;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AuctionResponse extends BaseResponse {
    private Long auctionId;
    private Long productId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal highestBid;
    private String status;
}
