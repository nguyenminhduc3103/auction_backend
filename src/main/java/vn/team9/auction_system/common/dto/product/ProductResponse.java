package vn.team9.auction_system.common.dto.product;

import vn.team9.auction_system.common.base.BaseResponse;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductResponse extends BaseResponse {
    private Long productId;
    private String name;
    private String description;
    private BigDecimal startingPrice;
    private BigDecimal currentPrice;
    private String status;
    private LocalDateTime createdAt;
}
