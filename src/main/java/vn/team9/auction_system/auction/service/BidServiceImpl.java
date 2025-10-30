package vn.team9.auction_system.auction.service;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Transactional
public class BidServiceImpl implements IBidService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;

    @Override
    public BidResponse placeBid(BidRequest request) {
        //Lấy auction và kiểm tra
        Auction auction = auctionRepository.findById(request.getAuctionId())
                .orElseThrow(() -> new RuntimeException("Auction not found"));
        if (!"OPEN".equalsIgnoreCase(auction.getStatus())) {
            throw new RuntimeException("Auction is not open for bidding");
        }

        //Lấy user đặt bid
        User bidder = userRepository.findById(request.getBidderId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        //Kiểm tra giá hợp lệ
        BigDecimal currentHighest = auction.getHighestCurrentPrice() != null
                ? auction.getHighestCurrentPrice()
                : BigDecimal.ZERO;

        if (request.getBidAmount().compareTo(currentHighest) <= 0) {
            throw new RuntimeException("Bid amount must be higher than current highest bid");
        }

        //Hủy cờ isHighest cũ
        auction.getBids().forEach(b -> b.setIsHighest(false));

        //Tạo Bid mới
        Bid bid = new Bid();
        bid.setAuction(auction);
        bid.setBidder(bidder);
        bid.setBidAmount(request.getBidAmount());
        bid.setCreatedAt(LocalDateTime.now());
        bid.setIsHighest(true);
        bid.setIsAuto(false);
        bidRepository.save(bid);

        //Cập nhật auction
        auction.setHighestCurrentPrice(request.getBidAmount());
        auctionRepository.save(auction);

        //Trả về response
        return mapToResponse(bid);
    }

    @Override
    public List<BidResponse> getBidsByAuction(Long auctionId) {
        List<Bid> bids = bidRepository.findByAuction_AuctionId(auctionId);
        return bids.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public BidResponse getHighestBid(Long auctionId) {
        Bid bid = bidRepository.findTopByAuction_AuctionIdOrderByBidAmountDesc(auctionId)
                .orElseThrow(() -> new RuntimeException("No bids found"));
        return mapToResponse(bid);
    }

    @Override
    public List<BidResponse> getBidsByUser(Long userId) {
        List<Bid> bids = bidRepository.findByBidder_UserId(userId);
        return bids.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public BidResponse placeAutoBid(BidRequest request) {
        // Giống placeBid, nhưng có auto-bid logic
        Auction auction = auctionRepository.findById(request.getAuctionId())
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        User bidder = userRepository.findById(request.getBidderId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal currentHighest = auction.getHighestCurrentPrice() != null
                ? auction.getHighestCurrentPrice()
                : BigDecimal.ZERO;

        if (request.getMaxAutoBidAmount().compareTo(currentHighest) <= 0) {
            throw new RuntimeException("Max auto bid must be greater than current highest bid");
        }

        Bid bid = new Bid();
        bid.setAuction(auction);
        bid.setBidder(bidder);
        bid.setBidAmount(currentHighest.add(request.getStepAutoBidAmount()));
        bid.setMaxAutobidAmount(request.getMaxAutoBidAmount());
        bid.setStepAutoBidAmount(request.getStepAutoBidAmount());
        bid.setIsAuto(true);
        bid.setIsHighest(true);
        bid.setCreatedAt(LocalDateTime.now());
        bidRepository.save(bid);

        auction.setHighestCurrentPrice(bid.getBidAmount());
        auctionRepository.save(auction);

        return mapToResponse(bid);
    }

    //map Entity → DTO
    private BidResponse mapToResponse(Bid bid) {
        BidResponse res = new BidResponse();
        res.setId(bid.getBidId());
        res.setAuctionId(bid.getAuction().getAuctionId());
        res.setBidderId(bid.getBidder().getUserId());
        res.setBidAmount(bid.getBidAmount());
        res.setMaxAutoBidAmount(bid.getMaxAutobidAmount());
        res.setStepAutoBidAmount(bid.getStepAutoBidAmount());
        res.setCreatedAt(bid.getCreatedAt());
        return res;
    }
}
