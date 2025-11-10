package vn.team9.auction_system.product.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.team9.auction_system.common.dto.product.ProductCreateRequest;
import vn.team9.auction_system.common.dto.product.ProductResponse;
import vn.team9.auction_system.common.dto.product.ProductUpdateRequest;
import vn.team9.auction_system.common.service.IProductService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

	private final IProductService productService;
	//tạo product
	@PostMapping
	public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductCreateRequest request) {
	return ResponseEntity.ok(productService.createProduct(Objects.requireNonNull(request)));
	}
	//cập nhật product
	@PutMapping("/{id}")
	public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
														 @RequestBody ProductUpdateRequest request) {
	return ResponseEntity.ok(productService.updateProduct(Objects.requireNonNull(id), request));
	}
	//lấy tất cả product
	@GetMapping
	public ResponseEntity<List<ProductResponse>> getAllProducts() {
		return ResponseEntity.ok(productService.getAllProducts());
	}
	//lấy product theo trang(api cho list product trong giao diện của seller)
	@GetMapping("/page")
	public ResponseEntity<Page<ProductResponse>> getProductsPage(@RequestParam(defaultValue = "0") int page) {
		return ResponseEntity.ok(productService.getProductsPage(page, 10));
	}
	//lấy product theo id
	@GetMapping("/{id}")
	public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
	return ResponseEntity.ok(productService.getProductById(Objects.requireNonNull(id)));
	}
	//xóa product
	@DeleteMapping("/{id}")
	public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id) {
		ProductResponse deleted = productService.deleteProduct(Objects.requireNonNull(id));
		Map<String, Object> response = new HashMap<>();
		response.put("message", "Product has been deleted successfully.");
		response.put("productId", deleted.getProductId());
		response.put("deletedAt", deleted.getDeletedAt());
		return ResponseEntity.ok(response);
	}
	//lấy product theo seller
	@GetMapping("/seller/{sellerId}")
	public ResponseEntity<List<ProductResponse>> getProductsBySeller(@PathVariable Long sellerId) {
	return ResponseEntity.ok(productService.getProductsBySeller(Objects.requireNonNull(sellerId)));
	}
}
