package vn.team9.auction_system.product.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import vn.team9.auction_system.common.dto.image.ImageRequest;
import vn.team9.auction_system.common.dto.image.ImageResponse;
import vn.team9.auction_system.common.dto.product.ProductCreateRequest;
import vn.team9.auction_system.common.dto.product.ProductResponse;
import vn.team9.auction_system.common.dto.product.ProductUpdateRequest;
import vn.team9.auction_system.product.model.Image;
import vn.team9.auction_system.product.model.Product;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    // ProductCreateRequest -> Product entity
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Product toEntity(ProductCreateRequest request);

    // ProductUpdateRequest -> Product entity (partial update)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(@MappingTarget Product product, ProductUpdateRequest request);

    // Product entity -> ProductResponse
    @Mapping(source = "seller.userId", target = "sellerId")
    @Mapping(source = "images", target = "images")
    @Mapping(target = "success", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    ProductResponse toResponse(Product entity);

    // ImageRequest -> Image entity
    @Mapping(target = "imageId", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(source = "imageUrl", target = "url")
    @Mapping(source = "thumbnail", target = "isThumbnail")
    Image toEntity(ImageRequest request);

    // Image entity -> ImageResponse
    @Mapping(source = "imageId", target = "id")
    @Mapping(source = "product.productId", target = "productId")
    @Mapping(source = "url", target = "imageUrl")
    @Mapping(source = "isThumbnail", target = "thumbnail")
    ImageResponse toResponse(Image entity);

    // Helpers for mapping collections
    java.util.List<Image> toImageEntities(java.util.List<ImageRequest> requests);
    java.util.List<ImageResponse> toImageResponses(java.util.List<Image> entities);
}
