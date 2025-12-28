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
import vn.team9.auction_system.common.dto.product.ProductApprovalRequest;
import vn.team9.auction_system.common.dto.product.ProductCreateRequest;
import vn.team9.auction_system.common.dto.product.ProductResponse;
import vn.team9.auction_system.common.dto.product.ProductUpdateRequest;
import vn.team9.auction_system.common.service.IProductService;
import vn.team9.auction_system.product.mapper.ProductMapper;
import vn.team9.auction_system.product.model.Image;
import vn.team9.auction_system.product.model.Product;
import vn.team9.auction_system.product.repository.ProductRepository;
import vn.team9.auction_system.auction.repository.AuctionRepository;
import vn.team9.auction_system.user.model.User;
import vn.team9.auction_system.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService implements IProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;
    private final AuctionRepository auctionRepository;
    private final vn.team9.auction_system.auction.service.AuctionNotificationService auctionNotificationService;

    // =========================
    // CREATE
    // =========================

    @Override
    public ProductResponse createProduct(@NonNull ProductCreateRequest request) {
        Product product = productMapper.toEntity(request);
        product.setSeller(getCurrentUser());
        product.setStatus("draft");
        product.setDeposit(null);
        product.setEstimatePrice(null);
        product.setCreatedAt(
                product.getCreatedAt() != null ? product.getCreatedAt() : LocalDateTime.now());

        applyImages(product, request.getImages());

        return saveAndMap(product);
    }

    // =========================
    // UPDATE
    // =========================

    @Override
    public ProductResponse updateProduct(@NonNull Long id, ProductUpdateRequest request) {
        Product product = getProductOrThrow(id);
        assertSellerOwnership(product);

        productMapper.updateEntity(product, request);

        if (request.getImages() != null) {
            applyImages(product, request.getImages());
        }

        return saveAndMap(product);
    }

    // =========================
    // QUERY
    // =========================

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(@NonNull Long id) {
        return productMapper.toResponse(getProductOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsPage(int page, int size) {
        Pageable pageable = buildPageable(page, size);
        return productRepository.findAllByIsDeletedFalse(pageable)
                .map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsBySellerPage(
            @NonNull Long sellerId, int page, int size) {
        Pageable pageable = buildPageable(page, size);
        return productRepository.findBySeller_UserIdAndIsDeletedFalse(sellerId, pageable)
                .map(productMapper::toResponse);
    }

    // =========================
    // DELETE (SOFT)
    // =========================

    @Override
    public ProductResponse deleteProduct(@NonNull Long id) {
        Product product = getProductOrThrow(id);
        product.setIsDeleted(true);
        product.setDeletedAt(LocalDateTime.now());
        return saveAndMap(product);
    }

    // =========================
    // APPROVAL FLOW
    // =========================

    @Override
    public ProductResponse requestApproval(@NonNull Long id) {
        Product product = getProductOrThrow(id);
        assertSellerOwnership(product);

        String status = normalize(product.getStatus());
        if ("pending".equals(status)) {
            return productMapper.toResponse(product);
        }
        if (!"draft".equals(status)) {
            throw new RuntimeException("Product current status cannot be submitted for approval");
        }

        product.setStatus("pending");
        product.setDeposit(null);
        product.setEstimatePrice(null);

        return saveAndMap(product);
    }

    @Override
    public ProductResponse approveProduct(@NonNull Long id, ProductApprovalRequest request) {
        Product product = getProductOrThrow(id);

        if (request.getDeposit() != null) {
            product.setDeposit(request.getDeposit());
        }
        if (request.getEstimatePrice() != null) {
            product.setEstimatePrice(request.getEstimatePrice());
        }

        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
            syncAuctionStatus(product, request.getStatus());
        }

        return saveAndMap(product);
    }

    // =========================
    // CORE HELPERS
    // =========================

    private Product getProductOrThrow(Long id) {
        return productRepository.findByProductIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    private void assertSellerOwnership(Product product) {
        User current = getCurrentUser();
        if (product.getSeller() == null ||
                !product.getSeller().getUserId().equals(current.getUserId())) {
            throw new RuntimeException("You do not have permission to modify this product");
        }
    }

    private ProductResponse saveAndMap(Product product) {
        return productMapper.toResponse(productRepository.save(product));
    }

    private Pageable buildPageable(int page, int size) {
        return PageRequest.of(
                Math.max(page, 0),
                size > 0 ? size : 10);
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    // =========================
    // AUCTION SYNC
    // =========================

    private void syncAuctionStatus(Product product, String productStatus) {
        System.out.println("\n🔍 [DEBUG] syncAuctionStatus called");
        System.out.println("   Product ID: " + product.getProductId());
        System.out.println("   Product Status: " + productStatus);

        auctionRepository.findAll().stream()
                .filter(a -> a.getProduct() != null
                        && a.getProduct().getProductId().equals(product.getProductId())
                        && "DRAFT".equalsIgnoreCase(a.getStatus()))
                .findFirst()
                .ifPresent(auction -> {
                    System.out.println("   Found DRAFT auction: " + auction.getAuctionId());

                    if ("approved".equalsIgnoreCase(productStatus)) {
                        System.out.println("   ✅ Setting auction status to PENDING");
                        auction.setStatus("PENDING");
                        auctionRepository.save(auction);

                        // 🚀 SEND NOTIFICATION TO SELLER
                        System.out.println("   🚀 Calling auctionNotificationService.notifySellerAuctionApproved");
                        try {
                            auctionNotificationService.notifySellerAuctionApproved(auction);
                            System.out.println("   ✅ Notification sent successfully!");
                        } catch (Exception e) {
                            System.out.println("   ❌ Failed to send notification: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else if ("rejected".equalsIgnoreCase(productStatus)) {
                        System.out.println("   ❌ Setting auction status to CANCELLED");
                        auction.setStatus("CANCELLED");
                        auctionRepository.save(auction);

                        // 🚀 SEND REJECTION NOTIFICATION TO SELLER
                        System.out.println("   🚀 Calling auctionNotificationService.notifySellerAuctionRejected");
                        try {
                            auctionNotificationService.notifySellerAuctionRejected(auction,
                                    "Admin đã từ chối yêu cầu đấu giá của bạn");
                            System.out.println("   ✅ Rejection notification sent successfully!");
                        } catch (Exception e) {
                            System.out.println("   ❌ Failed to send rejection notification: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
    }

    // =========================
    // IMAGE HANDLING
    // =========================

    private void applyImages(Product product, List<ImageRequest> images) {
        if (images == null)
            return;

        images.forEach(img -> {
            if (img.getSecureUrl() != null && !img.getSecureUrl().isBlank()) {
                img.setImageUrl(img.getSecureUrl());
            }
        });

        List<Image> imageEntities = productMapper.toImageEntities(images);
        imageEntities.forEach(img -> img.setProduct(product));

        if (product.getImages() == null) {
            product.setImages(new ArrayList<>());
        } else {
            product.getImages().clear();
        }

        product.getImages().addAll(imageEntities);
        syncPrimaryImage(product);
    }

    private void syncPrimaryImage(Product product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            product.setImageUrl(null);
            return;
        }

        product.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
                .map(Image::getUrl)
                .findFirst()
                .or(() -> product.getImages().stream().map(Image::getUrl).findFirst())
                .ifPresent(product::setImageUrl);
    }

    // =========================
    // SECURITY
    // =========================

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Please log in");
        }

        Object principal = auth.getPrincipal();
        String email = principal instanceof UserDetails ud
                ? ud.getUsername()
                : principal.toString();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
}
