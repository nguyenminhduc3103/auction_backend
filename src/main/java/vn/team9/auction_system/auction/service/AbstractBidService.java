package vn.team9.auction_system.auction.service;

import vn.team9.auction_system.auction.model.Auction;
import vn.team9.auction_system.auction.model.Bid;
import vn.team9.auction_system.auction.repository.AuctionRepository;
import vn.team9.auction_system.auction.repository.BidRepository;
import vn.team9.auction_system.common.dto.auction.BidResponse;
import vn.team9.auction_system.user.model.User;
import vn.team9.auction_system.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBidService {

    protected final AuctionRepository auctionRepository;
    protected final BidRepository bidRepository;
    protected final UserRepository userRepository;

    protected AbstractBidService(AuctionRepository auctionRepository,
                                 BidRepository bidRepository,
                                 UserRepository userRepository) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.userRepository = userRepository;
    }

    protected Auction findAuction(Long auctionId) {
        return auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));
    }

    protected User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    protected BidResponse mapToResponse(Bid bid) {
        return BidResponse.builder()
                .id(bid.getBidId())
                .auctionId(bid.getAuction().getAuctionId())
                .bidderId(bid.getBidder().getUserId())
                .bidAmount(bid.getBidAmount())
                .maxAutoBidAmount(bid.getMaxAutobidAmount())
                .stepAutoBidAmount(bid.getStepAutoBidAmount())
                .createdAt(bid.getCreatedAt())
                .success(true)
                .message("Success")
                .build();
    }

    protected void resetHighestBidFlags(Long auctionId) {
        List<Bid> currentBids = bidRepository.findByAuction_AuctionId(auctionId);
        List<Bid> bidsToUpdate = new ArrayList<>();

        for (Bid bid : currentBids) {
            if (Boolean.TRUE.equals(bid.getIsHighest())) {
                bid.setIsHighest(false);
                bidsToUpdate.add(bid);
            }
        }

        if (!bidsToUpdate.isEmpty()) {
            bidRepository.saveAll(bidsToUpdate);
        }
    }


}