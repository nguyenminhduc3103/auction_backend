package vn.team9.auction_system.common.service;

import vn.team9.auction_system.common.dto.auction.BidRequest;
import vn.team9.auction_system.common.dto.auction.BidResponse;
import java.util.List;

public interface IBidService {
    BidResponse placeBid(BidRequest request);
    List<BidResponse> getBidsByAuction(Long auctionId);
    BidResponse getHighestBid(Long auctionId);

    List<BidResponse> getBidsByUser(Long userId);           // Lấy tất cả bid của user
    BidResponse placeAutoBid(BidRequest request);
}
