package vn.team9.auction_system.auction.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import vn.team9.auction_system.auction.model.Auction;
import vn.team9.auction_system.auction.model.Bid;
import vn.team9.auction_system.auction.repository.AuctionRepository;
import vn.team9.auction_system.auction.repository.BidRepository;
import vn.team9.auction_system.common.dto.auction.BidRequest;
import vn.team9.auction_system.common.dto.auction.BidResponse;
import vn.team9.auction_system.common.service.IBidService;
import vn.team9.auction_system.feedback.event.NotificationEventPublisher;
import vn.team9.auction_system.transaction.service.AccountTransactionServiceImpl;
import vn.team9.auction_system.user.model.User;
import vn.team9.auction_system.user.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(isolation = Isolation.READ_COMMITTED)
public class BidServiceImpl extends AbstractBidService implements IBidService {

    private final IAutoBidService autoBidService;
    private final AccountTransactionServiceImpl transactionService;
    private final NotificationEventPublisher notificationPublisher;

    public BidServiceImpl(BidRepository bidRepository,
            AuctionRepository auctionRepository,
            UserRepository userRepository,
            IAutoBidService autoBidService,
            AccountTransactionServiceImpl transactionService,
            NotificationEventPublisher notificationPublisher) {
        super(auctionRepository, bidRepository, userRepository);
        this.autoBidService = autoBidService;
        this.transactionService = transactionService;
        this.notificationPublisher = notificationPublisher;
    }

    @Override
    public BidResponse placeBid(BidRequest request) {
        log.warn("=== START placeBid() ===");

        try {
            // STEP 1: Find auction
            Auction auction = findAuction(request.getAuctionId());

            // STEP 2: Find user
            User bidder = findUser(request.getBidderId());

            // STEP 3: Check auction status
            if (!"OPEN".equalsIgnoreCase(auction.getStatus())) {
                log.warn("Auction not OPEN: {}", auction.getStatus());
                return BidResponse.error("Auction is not open for bidding. Current status: " + auction.getStatus());
            }

            // STEP 4: Check seller cannot bid
            try {
                Long sellerId = auction.getProduct().getSeller().getUserId();
                if (bidder.getUserId().equals(sellerId)) {
                    log.warn("Seller trying to bid on own auction");
                    return BidResponse.error("Seller cannot bid on their own auction");
                }
            } catch (Exception e) {
                log.warn("Error checking seller: {}", e.getMessage());
            }

            // STEP 5: Get current highest price
            BigDecimal currentHighest = auction.getHighestCurrentPrice();
            if (currentHighest == null || currentHighest.compareTo(BigDecimal.ZERO) <= 0) {
                currentHighest = auction.getProduct().getStartPrice();
            }

            // STEP 6: Validate bid amount
            if (request.getBidAmount().compareTo(currentHighest) <= 0) {
                log.warn("Bid amount too low: {} <= {}", request.getBidAmount(), currentHighest);
                return BidResponse.error(
                        String.format("Bid amount (%s) must be higher than current highest bid (%s)",
                                request.getBidAmount(), currentHighest));
            }

            // STEP 7: Check user cannot bid continuously
            String continuousBidError = checkUserCanBidSafe(auction.getAuctionId(), bidder.getUserId());
            if (continuousBidError != null) {
                return BidResponse.error(continuousBidError);
            }

            // STEP 8: Validate balance
            String balanceError = validateBalanceForManualBidSafe(
                    bidder.getUserId(), request.getBidAmount(), auction.getAuctionId());
            if (balanceError != null) {
                return BidResponse.error(balanceError);
            }

            // STEP 9: Find previous highest bidder BEFORE resetting flags
            String auctionTitle = auction.getProduct().getName();
            Double bidAmountDouble = request.getBidAmount().doubleValue();
            Long sellerId = auction.getProduct().getSeller().getUserId();
            Long bidderId = bidder.getUserId();

            Optional<Bid> previousHighestOpt = bidRepository
                    .findByAuction_AuctionIdAndIsHighestTrue(auction.getAuctionId())
                    .stream()
                    .filter(b -> !b.getBidder().getUserId().equals(bidderId))
                    .findFirst();

            System.out.println("\n🔍 [DEBUG] Checking for previous highest bidder");
            System.out.println("   Current bidder: " + bidderId);
            System.out.println("   Previous highest found: " + previousHighestOpt.isPresent());
            if (previousHighestOpt.isPresent()) {
                System.out.println("   Previous bidder userId: " + previousHighestOpt.get().getBidder().getUserId());
                System.out
                        .println("   Previous bidder username: " + previousHighestOpt.get().getBidder().getUsername());
            }

            // STEP 10: Reset highest flags
            resetHighestBidFlags(auction.getAuctionId());

            // STEP 11: Create new bid
            Bid bid = new Bid();
            bid.setAuction(auction);
            bid.setBidder(bidder);
            bid.setBidAmount(request.getBidAmount());
            bid.setCreatedAt(LocalDateTime.now());
            bid.setIsHighest(true);
            bid.setIsAuto(false);

            bidRepository.save(bid);

            // STEP 12: Update auction
            auction.setHighestCurrentPrice(request.getBidAmount());
            auctionRepository.save(auction);

            // STEP 13: Send notifications
            try {
                // 1️⃣ Notify bidder: BID_PLACED
                notificationPublisher.publishBidPlacedNotification(
                        bidderId,
                        auctionTitle,
                        bidAmountDouble,
                        auction.getAuctionId());
                log.info("✅ BID_PLACED notification sent to bidder");

                // 2️⃣ Notify previous highest bidder: OUTBID
                if (previousHighestOpt.isPresent()) {
                    Bid previousBid = previousHighestOpt.get();
                    notificationPublisher.publishOutbidNotification(
                            previousBid.getBidder().getUserId(),
                            auctionTitle,
                            bidAmountDouble,
                            auction.getAuctionId());
                    log.info("✅ OUTBID notification sent to previous highest bidder");
                }

                // 3️⃣ Notify bidder: LEADING_BID
                notificationPublisher.publishHighestBidderNotification(
                        bidderId,
                        auctionTitle,
                        bidAmountDouble,
                        auction.getAuctionId());
                log.info("✅ LEADING_BID notification sent to new highest bidder");

                // 4️⃣ Notify seller: HIGHEST_BID_CHANGED
                if (!sellerId.equals(bidderId)) {
                    notificationPublisher.publishHighestBidderChangedNotification(
                            sellerId,
                            auctionTitle,
                            bidAmountDouble,
                            auction.getAuctionId());
                    log.info("✅ HIGHEST_BID_CHANGED notification sent to seller");
                }
            } catch (Exception e) {
                log.warn("⚠️ Failed to send bid notifications: {}", e.getMessage());
            }

            // STEP 14: Trigger auto-bid
            try {
                autoBidService.handleManualBid(auction.getAuctionId());
            } catch (Exception e) {
                log.warn("Auto-bid error: {}", e.getMessage());
            }

            log.warn("Bid placed successfully");

            // FIX: Use BidResponse.success() instead of mapToResponse()
            return BidResponse.success(bid, "Bid placed successfully");

        } catch (Exception e) {
            log.error("SYSTEM ERROR in placeBid: {}", e.getMessage(), e);
            return BidResponse.error("System error. Please try again.");
        }
    }

    @Override
    public List<BidResponse> getBidsByAuction(Long auctionId) {
        try {
            List<Bid> bids = bidRepository.findByAuction_AuctionId(auctionId);
            // mapToResponse() already sets success: true and message: "Success"
            return bids.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting bids: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public BidResponse getHighestBid(Long auctionId) {
        try {
            Optional<Bid> highestBidOpt = bidRepository
                    .findTopByAuction_AuctionIdOrderByBidAmountDesc(auctionId);

            if (highestBidOpt.isPresent()) {
                // Use mapToResponse() which already has success: true
                return mapToResponse(highestBidOpt.get());
            } else {
                // Return success with message "No bids found"
                return BidResponse.builder()
                        .success(true)
                        .message("No bids found")
                        .build();
            }
        } catch (Exception e) {
            log.error("Error getting highest bid: {}", e.getMessage());
            return BidResponse.error("Error getting highest bid");
        }
    }

    @Override
    public List<BidResponse> getBidsByUser(Long userId) {
        try {
            List<Bid> bids = bidRepository.findByBidder_UserId(userId);
            return bids.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting user bids: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * CHECK: User cannot bid continuously - SAFE VERSION
     */
    private String checkUserCanBidSafe(Long auctionId, Long userId) {
        try {
            Optional<Bid> currentHighestBid = bidRepository
                    .findTopByAuction_AuctionIdOrderByBidAmountDesc(auctionId);

            if (currentHighestBid.isPresent()) {
                Bid highestBid = currentHighestBid.get();
                if (highestBid.getBidder().getUserId().equals(userId)) {
                    return "You are currently the highest bidder. Please wait for someone else to bid before you can bid again.";
                }
            }
            return null; // No error
        } catch (Exception e) {
            log.warn("Error in checkUserCanBid: {}", e.getMessage());
            return "System error checking bid status.";
        }
    }

    /**
     * Check balance - SAFE VERSION
     */
    private String validateBalanceForManualBidSafe(Long userId, BigDecimal bidAmount, Long currentAuctionId) {
        try {
            BigDecimal withdrawable = transactionService.getWithdrawable(userId);

            Optional<Bid> currentHighestInThisAuction = bidRepository
                    .findTopByAuction_AuctionIdOrderByBidAmountDesc(currentAuctionId);

            boolean isCurrentHighestBidder = currentHighestInThisAuction.isPresent() &&
                    currentHighestInThisAuction.get().getBidder().getUserId().equals(userId);

            BigDecimal currentHighestAmount = isCurrentHighestBidder ? currentHighestInThisAuction.get().getBidAmount()
                    : BigDecimal.ZERO;

            BigDecimal availableForNewBid = isCurrentHighestBidder ? withdrawable.add(currentHighestAmount)
                    : withdrawable;

            if (availableForNewBid.compareTo(bidAmount) < 0) {
                if (isCurrentHighestBidder) {
                    return String.format("""
                            Insufficient credit to replace bid.
                            • Withdrawable balance: %s VND
                            • Amount locked in this auction: %s VND
                            • Total available: %s VND
                            • Required: %s VND""",
                            withdrawable, currentHighestAmount, availableForNewBid, bidAmount);
                } else {
                    return String.format("""
                            Not enough credit.
                            • Available: %s VND
                            • Require: %s VND""",
                            availableForNewBid, bidAmount);
                }
            }

            return null; // No error
        } catch (Exception e) {
            log.warn("Error in balance validation: {}", e.getMessage());
            return "System error checking balance.";
        }
    }
}