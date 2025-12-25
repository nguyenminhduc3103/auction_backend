package vn.team9.auction_system.auction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.team9.auction_system.auction.model.Auction;
import vn.team9.auction_system.auction.repository.AuctionRepository;
import vn.team9.auction_system.auction.repository.AuctionSpecification;
import vn.team9.auction_system.auction.repository.BidRepository;
import vn.team9.auction_system.common.dto.auction.AuctionRequest;
import vn.team9.auction_system.common.dto.auction.AuctionResponse;
import vn.team9.auction_system.common.service.IAuctionService;
import vn.team9.auction_system.auction.model.Bid;
import vn.team9.auction_system.product.model.Image;
import vn.team9.auction_system.product.model.Product;
import vn.team9.auction_system.product.repository.ProductRepository;
import vn.team9.auction_system.transaction.model.TransactionAfterAuction;
import vn.team9.auction_system.transaction.repository.TransactionAfterAuctionRepository;
import vn.team9.auction_system.user.model.User;
import vn.team9.auction_system.user.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Service
@RequiredArgsConstructor
@Transactional
public class AuctionServiceImpl implements IAuctionService {

    private final AuctionRepository auctionRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final TransactionAfterAuctionRepository transactionAfterAuctionRepository;
    private final BidRepository bidRepository;

    // Tạo phiên đấu giá mới (seller request)
    @Override
    public AuctionResponse createAuction(AuctionRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + request.getProductId()));

        // Kiểm tra product status phải là draft hoặc rejected
        String productStatus = product.getStatus() != null ? product.getStatus().toLowerCase() : "";
        if (!productStatus.equals("draft") && !productStatus.equals("rejected")) {
            throw new RuntimeException(
                    "Chỉ sản phẩm ở trạng thái 'draft' hoặc 'rejected' mới có thể tạo yêu cầu đấu giá.");
        }

        Auction auction = new Auction();
        auction.setProduct(product);
        auction.setStartTime(request.getStartTime());
        auction.setEndTime(request.getEndTime());
        auction.setStatus("DRAFT"); // Chờ admin duyệt
        auction.setHighestCurrentPrice(BigDecimal.ZERO);
        auction.setBidStepAmount(BigDecimal.valueOf(10000));// default step amount

        // Đổi product status sang PENDING (chờ admin duyệt)
        product.setStatus("pending");
        productRepository.save(product);

        Auction saved = auctionRepository.save(auction);
        return mapToResponse(saved);
    }

    // Cập nhật thông tin phiên đấu giá
    @Override
    public AuctionResponse updateAuction(Long id, AuctionRequest request) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Auction not found with id: " + id));

        auction.setStartTime(request.getStartTime());
        auction.setEndTime(request.getEndTime());
        auction.setStatus("UPDATED");

        Auction updated = auctionRepository.save(auction);
        return mapToResponse(updated);
    }

    // Xoá phiên đấu giá
    @Override
    public void deleteAuction(Long id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Auction not found with id: " + id));
        auctionRepository.delete(auction);
    }

    // Bắt đầu phiên đấu giá (Admin duyệt)
    @Override
    public void startAuction(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found with id: " + auctionId));

        if (!"PENDING".equals(auction.getStatus()))
            throw new RuntimeException("Only PENDING auctions can be started");

        auction.setStatus("OPEN");
        auction.setStartTime(LocalDateTime.now());
        auctionRepository.save(auction);
    }

    // Đóng phiên đấu giá (khi hết thời gian)
    @Override
    public void closeAuction(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found with id: " + auctionId));

        if (!"OPEN".equals(auction.getStatus()))
            throw new RuntimeException("Auction must be OPEN to close");

        auction.setStatus("CLOSED");
        auction.setEndTime(LocalDateTime.now());

        if (!auction.getBids().isEmpty()) {
            Bid highestBid = auction.getBids().stream()
                    .filter(b -> Boolean.TRUE.equals(b.getIsHighest()))
                    .findFirst()
                    .orElse(null);

            if (highestBid != null) {
                User winner = highestBid.getBidder();
                auction.setWinner(winner);

                // Tạo TransactionAfterAuction
                TransactionAfterAuction txn = new TransactionAfterAuction();
                txn.setAuction(auction);
                txn.setSeller(auction.getProduct().getSeller());
                txn.setBuyer(winner);
                txn.setAmount(highestBid.getBidAmount());
                txn.setStatus("PENDING"); // hoặc SUCCESS nếu thanh toán luôn
                transactionAfterAuctionRepository.save(txn);

                // Option: cập nhật balance
                User seller = auction.getProduct().getSeller();
                seller.setBalance(seller.getBalance().add(highestBid.getBidAmount()));
                userRepository.save(seller);
            }
        }

        auctionRepository.save(auction);
    }

    // Admin duyệt auction (DRAFT -> PENDING hoặc CANCELLED)
    @Override
    public AuctionResponse approveAuction(Long auctionId, String status) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found with id: " + auctionId));

        if (!"DRAFT".equals(auction.getStatus())) {
            throw new RuntimeException("Only DRAFT auctions can be approved or rejected");
        }

        String newStatus = status.toUpperCase();
        if (!"PENDING".equals(newStatus) && !"CANCELLED".equals(newStatus)) {
            throw new RuntimeException("Invalid status. Must be PENDING or CANCELLED");
        }

        auction.setStatus(newStatus);

        // Nếu approve auction thì đổi product status sang approved
        if ("PENDING".equals(newStatus)) {
            Product product = auction.getProduct();
            product.setStatus("approved");
            productRepository.save(product);
        }

        Auction saved = auctionRepository.save(auction);
        return mapToResponse(saved);
    }

    // Lấy thông tin phiên đấu giá theo ID
    @Override
    @Transactional(readOnly = true)
    public AuctionResponse getAuctionById(Long id) {
        Auction auction = auctionRepository.findByIdWithSellerAndImages(id)
                .orElseThrow(() -> new RuntimeException("Auction not found: " + id));

        return mapToResponse(auction);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<AuctionResponse> getAuctions(
            String status,
            String category,
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sort) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        sort.split(",")[1].equals("asc")
                                ? Sort.Direction.ASC
                                : Sort.Direction.DESC,
                        sort.split(",")[0]));

        Specification<Auction> spec = Specification.where(
                AuctionSpecification.hasStatus(status))
                .and(AuctionSpecification.excludeStatus("CLOSED"))
                .and(AuctionSpecification.hasCategory(category))
                .and(AuctionSpecification.hasKeyword(keyword))
                .and(AuctionSpecification.hasPriceRange(minPrice, maxPrice));

        Page<Auction> auctions = auctionRepository.findAll(spec, pageable);

        return auctions.map(this::mapToResponse);
    }

    // map Entity → DTO
    private AuctionResponse mapToResponse(Auction auction) {
        AuctionResponse res = new AuctionResponse();

        // Auction info
        res.setAuctionId(auction.getAuctionId());
        res.setStartTime(auction.getStartTime());
        res.setEndTime(auction.getEndTime());
        res.setHighestBid(auction.getHighestCurrentPrice());
        res.setStatus(auction.getStatus());
        res.setBidStepAmount(auction.getBidStepAmount());

        // Product
        Product product = auction.getProduct();
        res.setProductId(product.getProductId());
        res.setProductName(product.getName());
        res.setCategoryName(product.getCategory());
        res.setStartPrice(product.getStartPrice());
        res.setProductDescription(product.getDescription());
        res.setEstimatePrice(product.getEstimatePrice());

        // Images
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            // list
            List<String> urls = product.getImages().stream()
                    .map(Image::getUrl)
                    .collect(Collectors.toList());
            res.setProductImageUrls(urls);

            // thumbnail
            res.setProductImageUrl(
                    product.getImages().stream()
                            .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
                            .findFirst()
                            .orElse(product.getImages().getFirst())
                            .getUrl());
        }

        // Seller
        User seller = product.getSeller();
        if (seller != null) {
            res.setSellerId(seller.getUserId());
            res.setSellerName(seller.getFullName());
        }

        // totalBids
        res.setTotalBidders(bidRepository.countDistinctBidders(auction.getAuctionId()));
        return res;
    }

    // Lấy danh sách auctions của seller hiện tại (từ token)
    @Override
    public List<AuctionResponse> getAuctionsByCurrentSeller() {
        User currentUser = getCurrentUser();
        Long sellerId = currentUser.getUserId();

        // Tìm tất cả auctions mà product thuộc về seller này
        List<Auction> allAuctions = auctionRepository.findAll();
        List<Auction> sellerAuctions = allAuctions.stream()
                .filter(a -> a.getProduct() != null
                        && a.getProduct().getSeller() != null
                        && sellerId.equals(a.getProduct().getSeller().getUserId()))
                .collect(Collectors.toList());

        return sellerAuctions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Lấy danh sách auctions của một seller cụ thể (public - cho profile)
    @Override
    public List<AuctionResponse> getAuctionsBySellerId(Long sellerId) {
        List<Auction> allAuctions = auctionRepository.findAll();
        List<Auction> sellerAuctions = allAuctions.stream()
                .filter(a -> a.getProduct() != null
                        && a.getProduct().getSeller() != null
                        && sellerId.equals(a.getProduct().getSeller().getUserId()))
                .collect(Collectors.toList());

        return sellerAuctions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Helper: lấy user hiện tại từ SecurityContext
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
}
