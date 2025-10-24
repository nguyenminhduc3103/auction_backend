package vn.team9.auction_system.product.mapper;

import vn.team9.auction_system.product.model.Product;
import vn.team9.auction_system.product.model.Image;
import vn.team9.auction_system.common.dto.product.ProductRequest;
import vn.team9.auction_system.common.dto.product.ProductResponse;
import vn.team9.auction_system.common.dto.image.ImageRequest;
import vn.team9.auction_system.common.dto.image.ImageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    // ProductRequest -> Product entity
    Product toEntity(ProductRequest request);

    // Product entity -> ProductResponse
    @Mapping(source = "seller.userId", target = "sellerId")
    ProductResponse toResponse(Product entity);

    // ImageRequest -> Image entity
    Image toEntity(ImageRequest request);

    // Image entity -> ImageResponse
    @Mapping(source = "product.productId", target = "productId")
    ImageResponse toResponse(Image entity);
}
