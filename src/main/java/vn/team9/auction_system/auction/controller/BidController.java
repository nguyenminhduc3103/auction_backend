package vn.team9.auction_system.auction.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
@Slf4j
public class BidController {

    private final IBidService bidService;
    private final IAutoBidService autoBidService;

    // Place a manual bid
    @PostMapping
    public ResponseEntity<BidResponse> placeBid(@RequestBody BidRequest request) {
        log.warn("BidController.placeBid() called");
        BidResponse response = bidService.placeBid(request);
        log.warn("Response success: {}, message: {}",
                response.getSuccess(), response.getMessage());
        return ResponseEntity.ok(response);
    }

    // Place an automatic bid
    @PostMapping("/auto")
    public ResponseEntity<BidResponse> placeAutoBid(@RequestBody BidRequest request) {
        log.warn("BidController.placeAutoBid() called");
        BidResponse response = autoBidService.placeAutoBid(request);
        log.warn("AutoBid response success: {}, message: {}",
                response.getSuccess(), response.getMessage());
        return ResponseEntity.ok(response);
    }

    // Get bid list by auction session
    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<List<BidResponse>> getBidsByAuction(@PathVariable Long auctionId) {
        List<BidResponse> responses = bidService.getBidsByAuction(auctionId);
        log.warn("Got {} bids for auction {}", responses.size(), auctionId);
        return ResponseEntity.ok(responses);
    }

    // Get the highest bid in the auction session
    @GetMapping("/auction/{auctionId}/highest")
    public ResponseEntity<BidResponse> getHighestBid(@PathVariable Long auctionId) {
        BidResponse response = bidService.getHighestBid(auctionId);
        log.warn("Highest bid response success: {}, bidAmount: {}",
                response.getSuccess(), response.getBidAmount());
        return ResponseEntity.ok(response);
    }

    // Get bid list of a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BidResponse>> getBidsByUser(@PathVariable Long userId) {
        List<BidResponse> responses = bidService.getBidsByUser(userId);
        log.warn("Got {} bids for user {}", responses.size(), userId);
        return ResponseEntity.ok(responses);
    }
}