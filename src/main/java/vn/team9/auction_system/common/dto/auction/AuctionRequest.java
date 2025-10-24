package vn.team9.auction_system.common.dto.auction;

import vn.team9.auction_system.common.base.BaseRequest;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AuctionRequest extends BaseRequest {
    private Long productId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long createdBy;
}