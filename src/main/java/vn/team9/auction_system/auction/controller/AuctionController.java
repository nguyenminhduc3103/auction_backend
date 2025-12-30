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
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final IAuctionService auctionService;

    // Create auctions
    @PostMapping
    @PreAuthorize("hasAuthority('POST:/api/auctions')")
    public ResponseEntity<AuctionResponse> createAuction(@RequestBody AuctionRequest request) {
        return ResponseEntity.ok(auctionService.createAuction(request));
    }

    // get auctions by params
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

    // get auctions details
    @GetMapping("/{id}")
    public ResponseEntity<AuctionResponse> getAuctionById(@PathVariable Long id) {
        return ResponseEntity.ok(auctionService.getAuctionById(id));
    }

    // update auctions
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PUT:/api/auctions/{id}')")
    public ResponseEntity<AuctionResponse> updateAuction(@PathVariable Long id, @RequestBody AuctionRequest request) {
        return ResponseEntity.ok(auctionService.updateAuction(id, request));
    }

    // delete auctions
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE:/api/auctions/{id}')")
    public ResponseEntity<Void> deleteAuction(@PathVariable Long id) {
        auctionService.deleteAuction(id);
        return ResponseEntity.noContent().build();
    }

    // Open auctions by manual (admin)
    @PostMapping("/{auctionId}/start")
    @PreAuthorize("hasAuthority('POST:/api/auctions/{auctionId}/start')")
    public ResponseEntity<Void> startAuction(@PathVariable Long auctionId) {
        auctionService.startAuction(auctionId);
        return ResponseEntity.ok().build();
    }

    // Close auctions by manual (admin)
    @PostMapping("/{auctionId}/close")
    @PreAuthorize("hasAuthority('POST:/api/auctions/{auctionId}/close')")
    public ResponseEntity<Void> closeAuction(@PathVariable Long auctionId) {
        auctionService.closeAuction(auctionId);
        return ResponseEntity.ok().build();
    }

    // Admin approve Draft -> Pending/Cancel
    @PutMapping("/{auctionId}/approve")
    @PreAuthorize("hasAuthority('PUT:/api/auctions/{auctionId}/approve')")
    public ResponseEntity<AuctionResponse> approveAuction(
            @PathVariable Long auctionId,
            @RequestParam String status) {
        return ResponseEntity.ok(auctionService.approveAuction(auctionId, status));
    }

    // Get auctions from seller
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('GET:/api/auctions/me')")
    public ResponseEntity<?> getMyAuctions() {
        return ResponseEntity.ok(auctionService.getAuctionsByCurrentSeller());
    }

    // Lấy danh sách auctions của một seller cụ thể (public - cho profile)
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<?> getAuctionsBySellerId(@PathVariable Long sellerId) {
        return ResponseEntity.ok(auctionService.getAuctionsBySellerId(sellerId));
    }

}
