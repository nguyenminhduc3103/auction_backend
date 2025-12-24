package vn.team9.auction_system.common.service;

import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import vn.team9.auction_system.common.dto.auction.AuctionRequest;
import vn.team9.auction_system.common.dto.auction.AuctionResponse;

import java.math.BigDecimal;
import java.util.List;

public interface IAuctionService {

    // CRUD
    AuctionResponse createAuction(AuctionRequest request);

    AuctionResponse getAuctionById(Long id);

    AuctionResponse updateAuction(Long id, AuctionRequest request);

    void deleteAuction(Long id);

    // Mở đóng aution
    void startAuction(Long auctionId);

    void closeAuction(Long auctionId);

    // Admin duyệt auction (DRAFT -> PENDING hoặc CANCELLED)
    AuctionResponse approveAuction(Long auctionId, String status);

    // Lấy danh sách aution theo filler
    Page<AuctionResponse> getAuctions(
            String status,
            String category,
            String keyword, // sort theo tên sản phẩm
            BigDecimal minPrice, // Giá hiện tại của món hàng
            BigDecimal maxPrice,
            int page,
            int size,
            String sort);

    // Lấy danh sách auctions của seller hiện tại (từ token)
    List<AuctionResponse> getAuctionsByCurrentSeller();

    public Page<AuctionResponse> getParticipatingOpenAuctions(
            Long userId,
            int page,
            int size,
            String sort
    );
}
