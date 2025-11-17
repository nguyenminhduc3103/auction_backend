package vn.team9.auction_system.product.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import vn.team9.auction_system.common.dto.product.ProductCreateRequest;
import vn.team9.auction_system.common.dto.product.ProductResponse;
import vn.team9.auction_system.common.dto.product.ProductUpdateRequest;
import vn.team9.auction_system.common.service.IProductService;
import vn.team9.auction_system.user.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

	private final IProductService productService;
	private final UserRepository userRepository;
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
	//lấy product theo trang(api cho list product trong giao diện của bidder)
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

	// lấy product theo seller (lấy seller từ token) có phân trang
	@GetMapping("/seller/me/page")
	public ResponseEntity<Page<ProductResponse>> getMyProductsPage(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size
	) {
		Long sellerId = getCurrentUserId();
		return ResponseEntity.ok(productService.getProductsBySellerPage(Objects.requireNonNull(sellerId), page, size));
	}

	private Long getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new RuntimeException("Vui lòng đăng nhập");
		}
		String email;
		Object principal = authentication.getPrincipal();
		if (principal instanceof UserDetails userDetails) {
			email = userDetails.getUsername();
		} else if (principal instanceof String s) {
			email = s;
		} else {
			throw new RuntimeException("Không thể xác định người dùng hiện tại");
		}
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email))
				.getUserId();
	}
}
