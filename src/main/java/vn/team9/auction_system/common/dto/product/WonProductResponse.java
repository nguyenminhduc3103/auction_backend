package vn.team9.auction_system.common.dto.product;

import lombok.Data;
import vn.team9.auction_system.common.dto.image.ImageResponse;
import vn.team9.auction_system.product.model.Image;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class WonProductResponse {
    private Long transactionId;
    private String transactionStatus;
    private BigDecimal amount;
    private LocalDateTime updatedAt;

    private Long auctionId;
    private String auctionStatus;

    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal productPrice;
    private String productDescription;
    private String productCategory;

    private String sellerName;
    private Long sellerId;
}
