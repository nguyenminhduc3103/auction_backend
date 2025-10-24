package vn.team9.auction_system.common.service;

import vn.team9.auction_system.common.dto.auction.AuctionRequest;
import vn.team9.auction_system.common.dto.auction.AuctionResponse;
import java.util.List;

public interface IAuctionService {
    AuctionResponse createAuction(AuctionRequest request);
    AuctionResponse getAuctionById(Long id);
    List<AuctionResponse> getAllAuctions();
    AuctionResponse updateAuction(Long id, AuctionRequest request);
    void deleteAuction(Long id);

    void startAuction(Long auctionId);
    void closeAuction(Long auctionId);
    List<AuctionResponse> getActiveAuctions();
}
