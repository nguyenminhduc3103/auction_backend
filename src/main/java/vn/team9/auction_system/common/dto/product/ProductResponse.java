package vn.team9.auction_system.common.dto.product;

import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.team9.auction_system.common.base.BaseResponse;
import vn.team9.auction_system.common.dto.image.ImageResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductResponse extends BaseResponse {
    private Long productId;
    private Long sellerId;
    private String name;
    private String categories;
    private String description;
    private BigDecimal startPrice;
    private BigDecimal estimatePrice;
    private BigDecimal deposit;
    private String imageUrl;
    private String status;
    private LocalDateTime createdAt;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private List<ImageResponse> images;
}
