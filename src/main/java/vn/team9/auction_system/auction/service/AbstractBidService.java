package vn.team9.auction_system.auction.service;

import vn.team9.auction_system.auction.model.Auction;
import vn.team9.auction_system.auction.model.Bid;
import vn.team9.auction_system.auction.repository.AuctionRepository;
import vn.team9.auction_system.auction.repository.BidRepository;
import vn.team9.auction_system.common.dto.auction.BidResponse;
import vn.team9.auction_system.user.model.User;
import vn.team9.auction_system.user.repository.UserRepository;

import java.util.List;

public abstract class AbstractBidService {

    protected final AuctionRepository auctionRepository;
    protected final BidRepository bidRepository;
    protected final UserRepository userRepository;

    //Constructor protected có tham số
    protected AbstractBidService(AuctionRepository auctionRepository,
                                 BidRepository bidRepository,
                                 UserRepository userRepository) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.userRepository = userRepository;
    }

    //Các hàm tiện ích dùng chung
    protected Auction findAuction(Long auctionId) {
        return auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));
    }

    protected User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    protected BidResponse mapToResponse(Bid bid) {
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

    protected void resetHighestBidFlags(Long auctionId) {
        List<Bid> currentBids = bidRepository.findByAuction_AuctionId(auctionId);
        for (Bid bid : currentBids) {
            if (bid.getIsHighest()) {
                bid.setIsHighest(false);
                bidRepository.save(bid);
            }
        }
    }
}
