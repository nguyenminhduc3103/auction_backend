package vn.team9.auction_system.auction.service;

import vn.team9.auction_system.common.dto.auction.BidRequest;
import vn.team9.auction_system.common.dto.auction.BidResponse;

public interface IAutoBidService {
    BidResponse placeAutoBid(BidRequest request);
    void handleManualBid(Long auctionId);
}
