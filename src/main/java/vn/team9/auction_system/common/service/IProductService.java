package vn.team9.auction_system.common.service;

import org.springframework.lang.NonNull;
import vn.team9.auction_system.common.dto.product.ProductCreateRequest;
import vn.team9.auction_system.common.dto.product.ProductResponse;
import vn.team9.auction_system.common.dto.product.ProductUpdateRequest;

import java.util.List;

public interface IProductService {
    ProductResponse createProduct(@NonNull ProductCreateRequest request);
    ProductResponse updateProduct(@NonNull Long id, ProductUpdateRequest request);
    ProductResponse getProductById(@NonNull Long id);
    List<ProductResponse> getAllProducts();
    void deleteProduct(@NonNull Long id);

    List<ProductResponse> getProductsBySeller(@NonNull Long sellerId);
}
