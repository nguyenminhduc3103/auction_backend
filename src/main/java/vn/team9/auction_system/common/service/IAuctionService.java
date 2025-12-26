package vn.team9.auction_system.common.service;

import org.springframework.data.domain.Page;
import vn.team9.auction_system.common.dto.auction.AuctionRequest;
import vn.team9.auction_system.common.dto.auction.AuctionResponse;

import java.math.BigDecimal;
import java.util.List;

public interface IAuctionService {

    // CRUD operations
    AuctionResponse createAuction(AuctionRequest request);

    AuctionResponse getAuctionById(Long id);

    AuctionResponse updateAuction(Long id, AuctionRequest request);

    void deleteAuction(Long id);

    // Open/close auction
    void startAuction(Long auctionId);

    void closeAuction(Long auctionId);

    // Admin approves auction (DRAFT -> PENDING or CANCELLED)
    AuctionResponse approveAuction(Long auctionId, String status);

    // Get auction list with filters
    Page<AuctionResponse> getAuctions(
            String status,
            String category,
            String keyword, // Sort by product name
            BigDecimal minPrice, // Current price of the item
            BigDecimal maxPrice,
            int page,
            int size,
            String sort);

    // Get auction list of current seller (from token)
    List<AuctionResponse> getAuctionsByCurrentSeller();

    // Get auctions by seller ID (public - for seller profile)
    List<AuctionResponse> getAuctionsBySellerId(Long sellerId);

    // Get participating open auctions for user
    Page<AuctionResponse> getParticipatingOpenAuctions(
            Long userId,
            int page,
            int size,
            String sort);
}
