package vn.team9.auction_system.product.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.team9.auction_system.common.dto.product.ProductCreateRequest;
import vn.team9.auction_system.common.dto.product.ProductResponse;
import vn.team9.auction_system.common.dto.product.ProductUpdateRequest;
import vn.team9.auction_system.common.service.IProductService;

import java.util.List;
import java.util.Objects;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

	private final IProductService productService;

	@PostMapping
	public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductCreateRequest request) {
	return ResponseEntity.ok(productService.createProduct(Objects.requireNonNull(request)));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
														 @RequestBody ProductUpdateRequest request) {
	return ResponseEntity.ok(productService.updateProduct(Objects.requireNonNull(id), request));
	}

	@GetMapping
	public ResponseEntity<List<ProductResponse>> getAllProducts() {
		return ResponseEntity.ok(productService.getAllProducts());
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
	return ResponseEntity.ok(productService.getProductById(Objects.requireNonNull(id)));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
	productService.deleteProduct(Objects.requireNonNull(id));
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/seller/{sellerId}")
	public ResponseEntity<List<ProductResponse>> getProductsBySeller(@PathVariable Long sellerId) {
	return ResponseEntity.ok(productService.getProductsBySeller(Objects.requireNonNull(sellerId)));
	}
}
