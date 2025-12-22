package vn.team9.auction_system.product.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.team9.auction_system.common.dto.product.ProductCreateRequest;
import vn.team9.auction_system.common.dto.product.ProductResponse;
import vn.team9.auction_system.common.dto.product.ProductUpdateRequest;
import vn.team9.auction_system.common.dto.product.ProductApprovalRequest;
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

	// tạo product
	@PostMapping
	@PreAuthorize("hasAuthority('POST:/api/products')")
	public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductCreateRequest request) {
		return ResponseEntity.ok(productService.createProduct(Objects.requireNonNull(request)));
	}

	// cập nhật product
	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('PUT:/api/products/{id}')")
	public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
			@RequestBody ProductUpdateRequest request) {
		return ResponseEntity.ok(productService.updateProduct(Objects.requireNonNull(id), request));
	}

	// lấy product theo trang(api cho list product trong giao diện của bidder)
	@GetMapping("/page")
	public ResponseEntity<Page<ProductResponse>> getProductsPage(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(productService.getProductsPage(page, size));
	}

	// lấy product theo id
	@GetMapping("/{id}")
	public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
		return ResponseEntity.ok(productService.getProductById(Objects.requireNonNull(id)));
	}

	// xóa product
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAuthority('DELETE:/api/products/{id}')")
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
	@PreAuthorize("hasAuthority('GET:/api/products/seller/me/page')")
	public ResponseEntity<Page<ProductResponse>> getMyProductsPage(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Long sellerId = getCurrentUserId();
		return ResponseEntity.ok(productService.getProductsBySellerPage(Objects.requireNonNull(sellerId), page, size));
	}

	// Admin only: Approve product and set deposit + estimatePrice
	// TODO: Add @PreAuthorize("hasRole('ADMIN')") when RBAC is implemented
	@PutMapping("/{id}/approve")
	@PreAuthorize("hasAuthority('PUT:/api/products/{id}/approve')")
	public ResponseEntity<ProductResponse> approveProduct(
			@PathVariable Long id,
			@RequestBody ProductApprovalRequest request) {
		return ResponseEntity.ok(productService.approveProduct(Objects.requireNonNull(id), request));
	}

	// Seller submits a product for admin approval (draft -> pending)
	@PostMapping("/{id}/approval-request")
	@PreAuthorize("hasAuthority('POST:/api/products/{id}/approval-request')")
	public ResponseEntity<ProductResponse> requestApproval(@PathVariable Long id) {
		return ResponseEntity.ok(productService.requestApproval(Objects.requireNonNull(id)));
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
