package vn.team9.auction_system.common.dto.auction;

import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.team9.auction_system.common.base.BaseResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AuctionResponse extends BaseResponse {
    private Long auctionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal highestBid;
    private String status;
    private BigDecimal bidStepAmount;

    // Product
    private Long productId;
    private String productName;
    private String productImageUrl;
    private List<String> productImageUrls;
    private String productDescription;
    private BigDecimal startPrice;
    private BigDecimal estimatePrice;

    // Category
    private String categoryName;
    // Seller
    private Long sellerId;
    private String sellerName;

    private Long totalBidders;
}
