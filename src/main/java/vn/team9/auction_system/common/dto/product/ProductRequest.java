package vn.team9.auction_system.common.dto.product;

import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.team9.auction_system.common.base.BaseRequest;

import java.math.BigDecimal;

@Deprecated
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductRequest extends BaseRequest {
    private String name;
    private String description;
    private BigDecimal startingPrice;
    private Long categoryId;
    private Long sellerId;
}