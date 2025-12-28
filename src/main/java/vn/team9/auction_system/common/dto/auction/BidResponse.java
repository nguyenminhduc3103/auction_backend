package vn.team9.auction_system.common.dto.auction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.team9.auction_system.auction.model.Bid;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidResponse {
    private Long id;
    private Long auctionId;
    private Long bidderId;
    private BigDecimal bidAmount;
    private BigDecimal maxAutoBidAmount;
    private BigDecimal stepAutoBidAmount;
    private LocalDateTime createdAt;

    // THÊM field message để FE hiển thị
    private String message;
    private Boolean success;

    // Factory method cho thành công
    public static BidResponse success(Bid bid, String message) {
        return BidResponse.builder()
                .id(bid.getBidId())
                .auctionId(bid.getAuction().getAuctionId())
                .bidderId(bid.getBidder().getUserId())
                .bidAmount(bid.getBidAmount())
                .maxAutoBidAmount(bid.getMaxAutobidAmount())
                .stepAutoBidAmount(bid.getStepAutoBidAmount())
                .createdAt(bid.getCreatedAt())
                .success(true)
                .message(message)
                .build();
    }

    // Factory method cho lỗi
    public static BidResponse error(String errorMessage) {
        return BidResponse.builder()
                .success(false)
                .message(errorMessage)
                .build();
    }
}