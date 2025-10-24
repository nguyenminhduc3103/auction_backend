package vn.team9.auction_system.common.service;

import vn.team9.auction_system.common.dto.product.ProductRequest;
import vn.team9.auction_system.common.dto.product.ProductResponse;
import java.util.List;

public interface IProductService {
    ProductResponse createProduct(ProductRequest request);
    ProductResponse updateProduct(Long id, ProductRequest request);
    ProductResponse getProductById(Long id);
    List<ProductResponse> getAllProducts();
    void deleteProduct(Long id);

    List<ProductResponse> getProductsBySeller(Long sellerId);
}
