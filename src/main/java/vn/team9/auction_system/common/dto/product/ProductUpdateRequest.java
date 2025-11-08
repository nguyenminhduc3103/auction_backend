package vn.team9.auction_system.common.dto.product;

import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.team9.auction_system.common.base.BaseRequest;
import vn.team9.auction_system.common.dto.image.ImageRequest;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductUpdateRequest extends BaseRequest {
    private Long sellerId;
    private String name;
    private String categories;
    private String description;
    private BigDecimal startPrice;
    private BigDecimal estimatePrice;
    private BigDecimal deposit;
    private String imageUrl;
    private String status;
    private List<ImageRequest> images;
}
