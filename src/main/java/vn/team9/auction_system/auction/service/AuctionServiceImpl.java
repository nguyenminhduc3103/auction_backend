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

@Service
@RequiredArgsConstructor
@Transactional
public class AuctionServiceImpl implements IAuctionService {

    private final AuctionRepository auctionRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final TransactionAfterAuctionRepository transactionAfterAuctionRepository;
    private final BidRepository bidRepository;


    //Tạo phiên đấu giá mới
    @Override
    public AuctionResponse createAuction(AuctionRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + request.getProductId()));

        Auction auction = new Auction();
        auction.setProduct(product);
        auction.setStartTime(request.getStartTime());
        auction.setEndTime(request.getEndTime());
        auction.setStatus("PENDING");
        auction.setHighestCurrentPrice(BigDecimal.ZERO);
        auction.setBidStepAmount("10000"); // default step amount

        Auction saved = auctionRepository.save(auction);
        return mapToResponse(saved);
    }

    //Cập nhật thông tin phiên đấu giá
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

    //Xoá phiên đấu giá
    @Override
    public void deleteAuction(Long id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Auction not found with id: " + id));
        auctionRepository.delete(auction);
    }

    //Bắt đầu phiên đấu giá (Admin duyệt)
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

    //Đóng phiên đấu giá (khi hết thời gian)
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

    //Lấy thông tin phiên đấu giá theo ID
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
            String sort
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        sort.split(",")[1].equals("asc")
                                ? Sort.Direction.ASC
                                : Sort.Direction.DESC,
                        sort.split(",")[0]
                )
        );

        Specification<Auction> spec = Specification.where(
                        AuctionSpecification.hasStatus(status)
                )
                .and(AuctionSpecification.excludeStatus("CLOSED"))
                .and(AuctionSpecification.hasCategory(category))
                .and(AuctionSpecification.hasKeyword(keyword))
                .and(AuctionSpecification.hasPriceRange(minPrice, maxPrice));

        Page<Auction> auctions = auctionRepository.findAll(spec, pageable);

        return auctions.map(this::mapToResponse);
    }

    //map Entity → DTO
    private AuctionResponse mapToResponse(Auction auction) {
        AuctionResponse res = new AuctionResponse();

        // Auction info
        res.setAuctionId(auction.getAuctionId());
        res.setStartTime(auction.getStartTime());
        res.setEndTime(auction.getEndTime());
        res.setHighestBid(auction.getHighestCurrentPrice());
        res.setStatus(auction.getStatus());

        // Product
        Product product = auction.getProduct();
        res.setProductId(product.getProductId());
        res.setProductName(product.getName());
        res.setCategoryName(product.getCategory());
        res.setStartPrice(product.getStartPrice());
        res.setProductDescription(product.getDescription());

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
                            .getUrl()
            );
        }

        // Seller
        User seller = product.getSeller();
        if (seller != null) {
            res.setSellerId(seller.getUserId());
            res.setSellerName(seller.getUsername());
        }

        // totalBids
        res.setTotalBidders(bidRepository.countDistinctBidders(auction.getAuctionId()));
        return res;
    }
}
