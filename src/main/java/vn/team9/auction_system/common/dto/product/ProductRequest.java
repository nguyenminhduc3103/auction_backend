package vn.team9.auction_system.common.dto.product;

import vn.team9.auction_system.common.base.BaseRequest;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductRequest extends BaseRequest {
    private String name;
    private String description;
    private BigDecimal startingPrice;
    private Long categoryId;
    private Long sellerId;
}