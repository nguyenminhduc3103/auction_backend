package vn.team9.auction_system.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.team9.auction_system.common.dto.image.ImageRequest;
import vn.team9.auction_system.common.dto.product.ProductCreateRequest;
import vn.team9.auction_system.common.dto.product.ProductResponse;
import vn.team9.auction_system.common.dto.product.ProductUpdateRequest;
import vn.team9.auction_system.common.service.IProductService;
import vn.team9.auction_system.product.mapper.ProductMapper;
import vn.team9.auction_system.product.model.Image;
import vn.team9.auction_system.product.model.Product;
import vn.team9.auction_system.product.repository.ProductRepository;
import vn.team9.auction_system.user.model.User;
import vn.team9.auction_system.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
 

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService implements IProductService {

	private final ProductRepository productRepository;
	private final UserRepository userRepository;
	private final ProductMapper productMapper;

	@Override
	public ProductResponse createProduct(@NonNull ProductCreateRequest request) {
		Product product = productMapper.toEntity(request);
		product.setSeller(getCurrentUser());
		product.setStatus(request.getStatus() != null ? request.getStatus() : "pending");
		if (product.getCreatedAt() == null) {
			product.setCreatedAt(LocalDateTime.now());
		}

		// Chuẩn bị lại danh sách ảnh (vd: upload cloud, cập nhật URL) trước khi gán cho product
			List<ImageRequest> processedImages = handleImageUploads(request.getImages());
			replaceImages(product, processedImages);
			syncPrimaryImage(product);

		Product saved = productRepository.save(Objects.requireNonNull(product));
		return productMapper.toResponse(saved);
	}

	@Override
	public ProductResponse updateProduct(@NonNull Long id, ProductUpdateRequest request) {
		Product product = productRepository.findByProductIdAndIsDeletedFalse(id)
				.orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

		// Enforce ownership: only the seller (current user) can update
		User current = getCurrentUser();
		if (product.getSeller() == null || !product.getSeller().getUserId().equals(current.getUserId())) {
			throw new RuntimeException("Bạn không có quyền sửa sản phẩm này");
		}

		productMapper.updateEntity(product, request);

		// Ignore any attempt to change seller via request; seller is bound to token

		if (request.getStatus() != null) {
			product.setStatus(request.getStatus());
		}

		if (request.getImages() != null) {
			List<ImageRequest> processedImages = handleImageUploads(request.getImages());
			replaceImages(product, processedImages);
			syncPrimaryImage(product);
		}

		Product updated = productRepository.save(Objects.requireNonNull(product));
		return productMapper.toResponse(updated);
	}

	@Override
	@Transactional(readOnly = true)
	public ProductResponse getProductById(@NonNull Long id) {
		Product product = productRepository.findByProductIdAndIsDeletedFalse(id)
				.orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
		return productMapper.toResponse(product);
	}

	// Removed non-paginated getAllProducts()

	@Override
	@Transactional(readOnly = true)
	public Page<ProductResponse> getProductsPage(int page, int size) {
		int pageIndex = Math.max(page, 0);
		int pageSize = size > 0 ? size : 10;
		Pageable pageable = PageRequest.of(pageIndex, pageSize);
		return productRepository.findAllByIsDeletedFalse(pageable)
				.map(productMapper::toResponse);
	}

	@Override
	public ProductResponse deleteProduct(@NonNull Long id) {
		Product product = productRepository.findByProductIdAndIsDeletedFalse(id)
				.orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
		product.setIsDeleted(true);
		product.setDeletedAt(LocalDateTime.now());
		Product deleted = productRepository.save(product);
		return productMapper.toResponse(deleted);
	}

	// Removed non-paginated getProductsBySeller()

	@Override
	@Transactional(readOnly = true)
	public Page<ProductResponse> getProductsBySellerPage(@NonNull Long sellerId, int page, int size) {
		int pageIndex = Math.max(page, 0);
		int pageSize = size > 0 ? size : 10;
		Pageable pageable = PageRequest.of(pageIndex, pageSize);
		return productRepository.findBySeller_UserIdAndIsDeletedFalse(sellerId, pageable)
				.map(productMapper::toResponse);
	}

	// Removed resolveSeller(Long) as seller is now bound to current token

	// Resolve current authenticated user from SecurityContext (email as username)
	private User getCurrentUser() {
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
				.orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));
	}

	// Hook để tích hợp upload ảnh lên cloud hoặc xử lý metadata trước khi lưu
	private List<ImageRequest> handleImageUploads(List<ImageRequest> imageRequests) {
		if (imageRequests == null) {
			return null;
		}

		imageRequests.forEach(imageRequest -> {
			if (imageRequest.getSecureUrl() != null && !imageRequest.getSecureUrl().isBlank()) {
				imageRequest.setImageUrl(imageRequest.getSecureUrl());
			}
		});

		return imageRequests;
	}

	private void replaceImages(Product product, List<ImageRequest> imageRequests) {
		if (imageRequests == null) {
			return;
		}

		List<Image> imageEntities = productMapper.toImageEntities(imageRequests);
		imageEntities.forEach(image -> image.setProduct(product));

		if (product.getImages() == null) {
			product.setImages(new ArrayList<>());
		} else {
			product.getImages().clear();
		}
		product.getImages().addAll(imageEntities);

		if (product.getImages().isEmpty()) {
			product.setImageUrl(null);
		}
	}

	private void syncPrimaryImage(Product product) {
		if (product.getImages() == null || product.getImages().isEmpty()) {
			product.setImageUrl(null);
			return;
		}

		product.getImages().stream()
				.filter(image -> Boolean.TRUE.equals(image.getIsThumbnail()))
				.map(Image::getUrl)
				.findFirst()
				.or(() -> product.getImages().stream().map(Image::getUrl).findFirst())
				.ifPresent(product::setImageUrl);
	}
}
