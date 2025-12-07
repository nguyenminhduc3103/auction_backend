package vn.team9.auction_system.auction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.team9.auction_system.auction.model.Auction;
import vn.team9.auction_system.auction.repository.AuctionRepository;
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

    //Lấy thông tin phiên đấu giá theo ID
    @Override
    @Transactional(readOnly = true)
    public AuctionResponse getAuctionById(Long id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Auction not found with id: " + id));
        return mapToResponse(auction);
    }

    //Lấy danh sách tất cả phiên đấu giá
    @Override
    @Transactional(readOnly = true)
    public List<AuctionResponse> getAllAuctions() {
        return auctionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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

    //Lấy danh sách các phiên đang hoạt động
    @Override
    @Transactional(readOnly = true)
    public List<AuctionResponse> getActiveAuctions() {
        return auctionRepository.findByStatus("OPEN").stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    //map Entity → DTO
    private AuctionResponse mapToResponse(Auction auction) {
        AuctionResponse res = new AuctionResponse();
        res.setAuctionId(auction.getAuctionId());
        res.setStartTime(auction.getStartTime());
        res.setEndTime(auction.getEndTime());
        res.setHighestBid(auction.getHighestCurrentPrice());
        res.setStatus(auction.getStatus());

        // Map ảnh sản phẩm
        if (auction.getProduct().getImages() != null && !auction.getProduct().getImages().isEmpty()) {
            List<String> urls = auction.getProduct().getImages().stream()
                    .map(Image::getUrl)   // lấy tất cả url
                    .collect(Collectors.toList());
            res.setProductImageUrls(urls);

            // Lấy ảnh thumbnail nếu có, nếu không có thì lấy ảnh đầu tiên
            res.setProductImageUrl(
                    auction.getProduct().getImages().stream()
                            .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
                            .findFirst()
                            .orElse(auction.getProduct().getImages().getFirst())
                            .getUrl()
            );
        }
        res.setStartPrice(auction.getProduct().getStartPrice());
        res.setEstimatePrice(auction.getProduct().getEstimatePrice());
        res.setStartTime(auction.getStartTime());
        res.setEndTime(auction.getEndTime());
        res.setHighestBid(auction.getHighestCurrentPrice());
        res.setStatus(auction.getStatus());
        return res;
    }
}
