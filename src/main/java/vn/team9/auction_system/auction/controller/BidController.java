package vn.team9.auction_system.auction.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.team9.auction_system.auction.service.IAutoBidService;
import vn.team9.auction_system.common.service.IBidService;
import vn.team9.auction_system.common.dto.auction.BidRequest;
import vn.team9.auction_system.common.dto.auction.BidResponse;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidController {

    private final IBidService bidService;
    private final IAutoBidService autoBidService;

    // Đặt giá thầu thủ công
    @PostMapping
    @PreAuthorize("hasAuthority('POST:/api/bids')")
    public ResponseEntity<BidResponse> placeBid(@RequestBody BidRequest request) {
        return ResponseEntity.ok(bidService.placeBid(request));
    }

    // Đặt giá thầu tự động
    @PostMapping("/auto")
    @PreAuthorize("hasAuthority('POST:/api/bids/auto')")
    public ResponseEntity<BidResponse> placeAutoBid(@RequestBody BidRequest request) {
        return ResponseEntity.ok(autoBidService.placeAutoBid(request));
    }

    // Lấy danh sách bid theo phiên đấu giá
    @GetMapping("/auction/{auctionId}")
    @PreAuthorize("hasAuthority('GET:/api/bids/auction/{auctionId}')")
    public ResponseEntity<List<BidResponse>> getBidsByAuction(@PathVariable Long auctionId) {
        return ResponseEntity.ok(bidService.getBidsByAuction(auctionId));
    }

    // Lấy giá cao nhất trong phiên đấu giá đó
    @GetMapping("/auction/{auctionId}/highest")
    @PreAuthorize("hasAuthority('GET:/api/bids/auction/{auctionId}/highest')")
    public ResponseEntity<BidResponse> getHighestBid(@PathVariable Long auctionId) {
        return ResponseEntity.ok(bidService.getHighestBid(auctionId));
    }

    // Lấy danh sách bid của 1 user
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('GET:/api/bids/user/{userId}')")
    public ResponseEntity<List<BidResponse>> getBidsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(bidService.getBidsByUser(userId));
    }
}

