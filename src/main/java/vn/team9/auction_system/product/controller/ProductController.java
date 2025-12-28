package vn.team9.auction_system.product.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.team9.auction_system.common.dto.product.*;
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

	// CRUD
	@PostMapping
	@PreAuthorize("hasAuthority('POST:/api/products')")
	public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductCreateRequest request) {
		return ResponseEntity.ok(productService.createProduct(Objects.requireNonNull(request)));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('PUT:/api/products/{id}')")
	public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
			@RequestBody ProductUpdateRequest request) {
		return ResponseEntity.ok(productService.updateProduct(id, request));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
		return ResponseEntity.ok(productService.getProductById(id));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAuthority('DELETE:/api/products/{id}')")
	public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id) {
		ProductResponse deleted = productService.deleteProduct(id);

		Map<String, Object> response = new HashMap<>();
		response.put("message", "Product has been deleted successfully");
		response.put("productId", deleted.getProductId());
		response.put("deletedAt", deleted.getDeletedAt());

		return ResponseEntity.ok(response);
	}

	// QUERY
	// Product list for bidder (all products)
	@GetMapping("/page")
	public ResponseEntity<Page<ProductResponse>> getProductsPage(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(productService.getProductsPage(page, size));
	}

	// Get products by seller ID (public - for seller profile)
	@GetMapping("/seller/{sellerId}/page")
	public ResponseEntity<Page<ProductResponse>> getProductsBySellerPage(
			@PathVariable Long sellerId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(productService.getProductsBySellerPage(Objects.requireNonNull(sellerId), page, size));
	}

	// Product list for current seller
	@GetMapping("/seller/me/page")
	@PreAuthorize("hasAuthority('GET:/api/products/seller/me/page')")
	public ResponseEntity<Page<ProductResponse>> getMyProductsPage(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Long sellerId = getCurrentUserId();
		return ResponseEntity.ok(
				productService.getProductsBySellerPage(sellerId, page, size));
	}

	// Admin only: Approve product and set deposit + estimatePrice
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

	// SECURITY
	private Long getCurrentUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null || !auth.isAuthenticated()) {
			throw new RuntimeException("Please log in");
		}

		Object principal = auth.getPrincipal();
		String email;

		if (principal instanceof UserDetails userDetails) {
			email = userDetails.getUsername();
		} else if (principal instanceof String s) {
			email = s;
		} else {
			throw new RuntimeException("Cannot identify current user");
		}

		return userRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("User not found with email: " + email))
				.getUserId();
	}
}
