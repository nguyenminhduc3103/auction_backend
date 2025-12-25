package vn.team9.auction_system.auction.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.team9.auction_system.common.service.IAuctionService;
import vn.team9.auction_system.common.dto.auction.AuctionRequest;
import vn.team9.auction_system.common.dto.auction.AuctionResponse;

import java.math.BigDecimal;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final IAuctionService auctionService;

    // Tạo phiên đấu giá
    @PostMapping
    @PreAuthorize("hasAuthority('POST:/api/auctions')")
    public ResponseEntity<AuctionResponse> createAuction(@RequestBody AuctionRequest request) {
        return ResponseEntity.ok(auctionService.createAuction(request));
    }

    // Lấy danh sách phiên đấu giá theo param
    @GetMapping
    public ResponseEntity<?> getAuctions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,

            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "startTime,asc") String sort) {
        return ResponseEntity.ok(
                auctionService.getAuctions(
                        status,
                        category,
                        keyword,
                        minPrice,
                        maxPrice,
                        page,
                        size,
                        sort));
    }

    // Lấy chi tiết 1 phiên đấu giá
    @GetMapping("/{id}")
    public ResponseEntity<AuctionResponse> getAuctionById(@PathVariable Long id) {
        return ResponseEntity.ok(auctionService.getAuctionById(id));
    }

    // Cập nhật đấu giá
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PUT:/api/auctions/{id}')")
    public ResponseEntity<AuctionResponse> updateAuction(@PathVariable Long id, @RequestBody AuctionRequest request) {
        return ResponseEntity.ok(auctionService.updateAuction(id, request));
    }

    // Xóa đấu giá
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE:/api/auctions/{id}')")
    public ResponseEntity<Void> deleteAuction(@PathVariable Long id) {
        auctionService.deleteAuction(id);
        return ResponseEntity.noContent().build();
    }

    // Bắt đầu đấu giá (OPEN)
    @PostMapping("/{auctionId}/start")
    @PreAuthorize("hasAuthority('POST:/api/auctions/{auctionId}/start')")
    public ResponseEntity<Void> startAuction(@PathVariable Long auctionId) {
        auctionService.startAuction(auctionId);
        return ResponseEntity.ok().build();
    }

    // Đóng đấu giá (CLOSE)
    @PostMapping("/{auctionId}/close")
    @PreAuthorize("hasAuthority('POST:/api/auctions/{auctionId}/close')")
    public ResponseEntity<Void> closeAuction(@PathVariable Long auctionId) {
        auctionService.closeAuction(auctionId);
        return ResponseEntity.ok().build();
    }

    // Admin duyệt auction (DRAFT -> PENDING hoặc CANCELLED)
    @PutMapping("/{auctionId}/approve")
    public ResponseEntity<AuctionResponse> approveAuction(
            @PathVariable Long auctionId,
            @RequestParam String status) {
        return ResponseEntity.ok(auctionService.approveAuction(auctionId, status));
    }

    // Lấy danh sách auctions của seller hiện tại (từ token)
    @GetMapping("/me")
    public ResponseEntity<?> getMyAuctions() {
        return ResponseEntity.ok(auctionService.getAuctionsByCurrentSeller());
    }

}
