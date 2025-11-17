package vn.team9.auction_system.common.dto.auction;

import vn.team9.auction_system.common.base.BaseResponse;
import lombok.Data;
import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AuctionResponse extends BaseResponse {
    private Long auctionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal highestBid;
    private String status;

    // Product
    private Long productId;
    private String productName;
    private String productImageUrl;
    private List<String> productImageUrls;
    private String productDescription;
    private BigDecimal startPrice;

    // Category
    private String categoryName;

    // Seller
    private Long sellerId;
    private String sellerName;

    private Long totalBidders;
}
