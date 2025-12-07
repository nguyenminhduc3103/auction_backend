package vn.team9.auction_system.common.dto.product;

import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.team9.auction_system.common.base.BaseRequest;
import vn.team9.auction_system.common.dto.image.ImageRequest;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductCreateRequest extends BaseRequest {
    private Long sellerId;
    private String name;
    @JsonAlias({ "categories" })
    private String category;
    private String description;
    private BigDecimal startPrice;
    // estimatePrice and deposit will be set by admin during approval
    // status will be automatically set to "pending" by system
    private String imageUrl;
    private List<ImageRequest> images;
}
