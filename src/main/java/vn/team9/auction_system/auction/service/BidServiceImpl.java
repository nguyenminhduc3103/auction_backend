package vn.team9.auction_system.auction.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.team9.auction_system.auction.model.Auction;
import vn.team9.auction_system.auction.model.Bid;
import vn.team9.auction_system.auction.repository.AuctionRepository;
import vn.team9.auction_system.auction.repository.BidRepository;
import vn.team9.auction_system.common.dto.auction.BidRequest;
import vn.team9.auction_system.common.dto.auction.BidResponse;
import vn.team9.auction_system.common.service.IBidService;
import vn.team9.auction_system.user.model.User;
import vn.team9.auction_system.user.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BidServiceImpl extends AbstractBidService implements IBidService {

    private final IAutoBidService autoBidService;

    public BidServiceImpl(BidRepository bidRepository,
                          AuctionRepository auctionRepository,
                          UserRepository userRepository,
                          IAutoBidService autoBidService) {
        super(auctionRepository, bidRepository, userRepository);
        this.autoBidService = autoBidService;
    }

    @Override
    public BidResponse placeBid(BidRequest request) {
        Auction auction = findAuction(request.getAuctionId());
        User bidder = findUser(request.getBidderId());

        if (!"OPEN".equalsIgnoreCase(auction.getStatus())) {
            throw new RuntimeException("Auction is not open for bidding");
        }

        BigDecimal currentHighest = auction.getHighestCurrentPrice();
        if (currentHighest == null || currentHighest.compareTo(BigDecimal.ZERO) <= 0) {
            currentHighest = auction.getProduct().getStartPrice();
        }

        if (request.getBidAmount().compareTo(currentHighest) <= 0) {
            throw new RuntimeException("Bid amount must be higher than current highest bid");
        }

        // Reset highest flag của tất cả bids cũ
        resetHighestBidFlags(auction.getAuctionId());

        // Tạo bid thường
        Bid bid = new Bid();
        bid.setAuction(auction);
        bid.setBidder(bidder);
        bid.setBidAmount(request.getBidAmount());
        bid.setCreatedAt(LocalDateTime.now());
        bid.setIsHighest(true);
        bid.setIsAuto(false);
        bidRepository.save(bid);

        auction.setHighestCurrentPrice(request.getBidAmount());
        auctionRepository.save(auction);

        // QUAN TRỌNG: Kích hoạt auto-bid competition sau khi đặt bid thủ công
        try {
            autoBidService.handleManualBid(auction.getAuctionId());
        } catch (Exception e) {
            // Không ảnh hưởng đến bid thủ công nếu auto-bid lỗi
            System.err.println("Auto-bid error: " + e.getMessage());
        }

        return mapToResponse(bid);
    }

    @Override
    public List<BidResponse> getBidsByAuction(Long auctionId) {
        return bidRepository.findByAuction_AuctionId(auctionId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public BidResponse getHighestBid(Long auctionId) {
        Bid bid = bidRepository.findTopByAuction_AuctionIdOrderByBidAmountDesc(auctionId)
                .orElseThrow(() -> new RuntimeException("No bids found"));
        return mapToResponse(bid);
    }

    @Override
    public List<BidResponse> getBidsByUser(Long userId) {
        return bidRepository.findByBidder_UserId(userId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }
}