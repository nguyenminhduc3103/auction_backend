package vn.team9.auction_system.product.mapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import vn.team9.auction_system.common.dto.image.ImageRequest;
import vn.team9.auction_system.common.dto.image.ImageResponse;
import vn.team9.auction_system.common.dto.product.ProductCreateRequest;
import vn.team9.auction_system.common.dto.product.ProductResponse;
import vn.team9.auction_system.common.dto.product.ProductUpdateRequest;
import vn.team9.auction_system.product.model.Image;
import vn.team9.auction_system.product.model.Product;
import vn.team9.auction_system.user.model.User;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-28T01:15:30+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public Product toEntity(ProductCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Product product = new Product();

        product.setCategory( request.getCategory() );
        product.setDescription( request.getDescription() );
        product.setImageUrl( request.getImageUrl() );
        product.setName( request.getName() );
        product.setStartPrice( request.getStartPrice() );

        return product;
    }

    @Override
    public void updateEntity(Product product, ProductUpdateRequest request) {
        if ( request == null ) {
            return;
        }

        if ( request.getCategory() != null ) {
            product.setCategory( request.getCategory() );
        }
        if ( request.getDescription() != null ) {
            product.setDescription( request.getDescription() );
        }
        if ( request.getImageUrl() != null ) {
            product.setImageUrl( request.getImageUrl() );
        }
        if ( request.getName() != null ) {
            product.setName( request.getName() );
        }
        if ( request.getStartPrice() != null ) {
            product.setStartPrice( request.getStartPrice() );
        }
    }

    @Override
    public ProductResponse toResponse(Product entity) {
        if ( entity == null ) {
            return null;
        }

        ProductResponse productResponse = new ProductResponse();

        productResponse.setSellerId( entitySellerUserId( entity ) );
        productResponse.setImages( toImageResponses( entity.getImages() ) );
        productResponse.setCategory( entity.getCategory() );
        productResponse.setCreatedAt( entity.getCreatedAt() );
        productResponse.setDeletedAt( entity.getDeletedAt() );
        productResponse.setDeposit( entity.getDeposit() );
        productResponse.setDescription( entity.getDescription() );
        productResponse.setEstimatePrice( entity.getEstimatePrice() );
        productResponse.setImageUrl( entity.getImageUrl() );
        productResponse.setIsDeleted( entity.getIsDeleted() );
        productResponse.setName( entity.getName() );
        productResponse.setProductId( entity.getProductId() );
        productResponse.setStartPrice( entity.getStartPrice() );
        productResponse.setStatus( entity.getStatus() );

        return productResponse;
    }

    @Override
    public Image toEntity(ImageRequest request) {
        if ( request == null ) {
            return null;
        }

        Image image = new Image();

        image.setUrl( request.getImageUrl() );
        image.setIsThumbnail( request.isThumbnail() );

        return image;
    }

    @Override
    public ImageResponse toResponse(Image entity) {
        if ( entity == null ) {
            return null;
        }

        ImageResponse imageResponse = new ImageResponse();

        imageResponse.setId( entity.getImageId() );
        imageResponse.setProductId( entityProductProductId( entity ) );
        imageResponse.setImageUrl( entity.getUrl() );
        if ( entity.getIsThumbnail() != null ) {
            imageResponse.setThumbnail( entity.getIsThumbnail() );
        }

        return imageResponse;
    }

    @Override
    public List<Image> toImageEntities(List<ImageRequest> requests) {
        if ( requests == null ) {
            return null;
        }

        List<Image> list = new ArrayList<Image>( requests.size() );
        for ( ImageRequest imageRequest : requests ) {
            list.add( toEntity( imageRequest ) );
        }

        return list;
    }

    @Override
    public List<ImageResponse> toImageResponses(List<Image> entities) {
        if ( entities == null ) {
            return null;
        }

        List<ImageResponse> list = new ArrayList<ImageResponse>( entities.size() );
        for ( Image image : entities ) {
            list.add( toResponse( image ) );
        }

        return list;
    }

    private Long entitySellerUserId(Product product) {
        if ( product == null ) {
            return null;
        }
        User seller = product.getSeller();
        if ( seller == null ) {
            return null;
        }
        Long userId = seller.getUserId();
        if ( userId == null ) {
            return null;
        }
        return userId;
    }

    private Long entityProductProductId(Image image) {
        if ( image == null ) {
            return null;
        }
        Product product = image.getProduct();
        if ( product == null ) {
            return null;
        }
        Long productId = product.getProductId();
        if ( productId == null ) {
            return null;
        }
        return productId;
    }
}
