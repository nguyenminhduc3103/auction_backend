package vn.team9.auction_system.common.dto.auction;

import vn.team9.auction_system.common.base.BaseResponse;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AuctionResponse extends BaseResponse {
    private Long auctionId;
    private Long productId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal highestBid;
    private String status;

    // Thêm thông tin product
    private String productName;
    private String productImageUrl;           // thumbnail hoặc ảnh đầu tiên
    private List<String> productImageUrls;    // nếu muốn trả tất cả ảnh

    private BigDecimal startPrice;            // giá khởi điểm
    private String estimatePrice;             // giá ước tính
}
