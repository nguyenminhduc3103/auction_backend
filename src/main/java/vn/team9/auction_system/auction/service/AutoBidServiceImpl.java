package vn.team9.auction_system.auction.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import vn.team9.auction_system.auction.event.AuctionEventPublisher;
import vn.team9.auction_system.auction.model.Auction;
import vn.team9.auction_system.auction.model.Bid;
import vn.team9.auction_system.auction.repository.AuctionRepository;
import vn.team9.auction_system.auction.repository.BidRepository;
import vn.team9.auction_system.common.dto.auction.BidRequest;
import vn.team9.auction_system.common.dto.auction.BidResponse;
import vn.team9.auction_system.transaction.service.AccountTransactionServiceImpl;
import vn.team9.auction_system.user.model.User;
import vn.team9.auction_system.user.repository.UserRepository;
import vn.team9.auction_system.feedback.event.NotificationEventPublisher;
import vn.team9.auction_system.auction.model.Auction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(isolation = Isolation.READ_COMMITTED)
public class AutoBidServiceImpl extends AbstractBidService implements IAutoBidService {

    private final AuctionEventPublisher eventPublisher;
    private final NotificationEventPublisher notificationPublisher;
    private final AccountTransactionServiceImpl transactionService;
    private static final int MAX_AUTO_BID_ITERATIONS = 10;

    public AutoBidServiceImpl(AuctionRepository auctionRepository,
                              BidRepository bidRepository,
                              UserRepository userRepository,
                              AuctionEventPublisher eventPublisher,
                              NotificationEventPublisher notificationPublisher,
                              AccountTransactionServiceImpl transactionService) {
        super(auctionRepository, bidRepository, userRepository);
        this.eventPublisher = eventPublisher;
        this.notificationPublisher = notificationPublisher;
        this.transactionService = transactionService;
    }

    @Override
    public BidResponse placeAutoBid(BidRequest request) {
        log.warn("=== START placeAutoBid() ===");
        log.warn("Request: auctionId={}, bidderId={}, maxAmount={}, step={}",
                request.getAuctionId(), request.getBidderId(),
                request.getMaxAutoBidAmount(), request.getStepAutoBidAmount());

        try {
            // STEP 1: Find auction
            log.warn("STEP 1: Finding auction {}...", request.getAuctionId());
            Auction auction = findAuction(request.getAuctionId());
            log.warn("Auction found: id={}, status={}",
                    auction.getAuctionId(), auction.getStatus());

            // STEP 2: Find user
            log.warn("STEP 2: Finding user {}...", request.getBidderId());
            User bidder = findUser(request.getBidderId());
            log.warn("User found: id={}, name={}",
                    bidder.getUserId(), bidder.getFullName());

            // STEP 3: Check auction status
            log.warn("STEP 3: Checking auction status...");
            if (!"OPEN".equalsIgnoreCase(auction.getStatus())) {
                log.warn("Auction status is '{}', not OPEN", auction.getStatus());
                return BidResponse.error("Auction is not open for bidding. Current status: " + auction.getStatus());
            }
            log.warn("Auction is OPEN");

            // STEP 4: Check seller cannot bid
            log.warn("STEP 4: Checking seller restriction...");
            try {
                Long sellerId = auction.getProduct().getSeller().getUserId();
                log.warn("Seller ID: {}, Bidder ID: {}", sellerId, bidder.getUserId());
                if (bidder.getUserId().equals(sellerId)) {
                    log.warn("Seller trying to auto-bid on own auction");
                    return BidResponse.error("Seller cannot bid on their own auction");
                }
                log.warn("User is not the seller");
            } catch (Exception e) {
                log.warn("Error checking seller: {}", e.getMessage());
                return BidResponse.error("Error checking auction seller information");
            }

            // STEP 5: Get current highest price
            log.warn("STEP 5: Getting current highest price...");
            BigDecimal currentHighest = auction.getHighestCurrentPrice();
            log.warn("Current highest from auction: {}", currentHighest);

            if (currentHighest == null || currentHighest.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("No current highest, using product start price");
                currentHighest = auction.getProduct().getStartPrice();
                log.warn("Product start price: {}", currentHighest);
            }

            // STEP 6: Validate auto-bid parameters
            log.warn("STEP 6: Validating auto-bid parameters...");
            BigDecimal maxAmount = request.getMaxAutoBidAmount();
            BigDecimal stepAmount = request.getStepAutoBidAmount();

            if (maxAmount == null || maxAmount.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Max auto-bid amount must be greater than 0");
                return BidResponse.error("Max auto-bid amount must be greater than 0");
            }

            if (stepAmount == null || stepAmount.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Step auto-bid amount must be greater than 0");
                return BidResponse.error("Step auto-bid amount must be greater than 0");
            }

            if (maxAmount.compareTo(currentHighest) <= 0) {
                log.warn("Max auto-bid must be greater than current highest");
                return BidResponse.error(
                        String.format("Max auto-bid amount (%s) must be greater than current highest bid (%s)",
                                maxAmount, currentHighest)
                );
            }

            log.warn("Auto-bid parameters valid: Max={}, Step={}", maxAmount, stepAmount);

            // STEP 7: Validate balance
            log.warn("STEP 7: Validating balance for max auto-bid amount...");
            String balanceError = validateBalanceForBidAmountSafe(
                    bidder.getUserId(), maxAmount, auction.getAuctionId());
            if (balanceError != null) {
                log.warn("Balance validation failed: {}", balanceError);
                return BidResponse.error(balanceError);
            }
            log.warn("Balance validation passed for max amount: {}", maxAmount);

            // STEP 8: Check existing auto-bid
            log.warn("STEP 8: Checking existing auto-bid...");
            Optional<Bid> existingAutoBid = bidRepository
                    .findTopByAuction_AuctionIdAndBidder_UserIdAndIsAutoTrueOrderByCreatedAtDesc(
                            auction.getAuctionId(), bidder.getUserId());

            if (existingAutoBid.isPresent()) {
                log.warn("User already has auto-bid. Deleting old one...");
                try {
                    bidRepository.delete(existingAutoBid.get());
                    log.warn("Old auto-bid deleted");
                } catch (Exception e) {
                    log.warn("Error deleting old auto-bid: {}", e.getMessage());
                    // Continue anyway
                }
            }

            // STEP 9: Create auto-bid configuration
            log.warn("STEP 9: Creating auto-bid configuration...");
            Bid autoBidConfig = createAutoBidConfig(auction, bidder, currentHighest, maxAmount, stepAmount);
            log.warn("Auto-bid config created: id={}", autoBidConfig.getBidId());

            // STEP 10: Trigger auto-bid competition
            log.warn("STEP 10: Triggering auto-bid competition...");
            try {
                triggerAutoBidCompetition(auction);
                log.warn("Auto-bid competition completed");
            } catch (Exception e) {
                log.warn("Auto-bid competition error (non-fatal): {}", e.getMessage());
                // Do not return error because auto-bid config was created successfully
            }

            log.warn("=== END placeAutoBid() - SUCCESS ===");
            return BidResponse.success(autoBidConfig, "Auto-bid configured successfully");

        } catch (Exception e) {
            log.warn("=== END placeAutoBid() - UNEXPECTED ERROR ===");
            log.warn("Unexpected error: {}", e.getMessage(), e);
            return BidResponse.error("System error configuring auto-bid. Please try again.");
        }
    }

    @Override
    public void handleManualBid(Long auctionId) {
        log.warn("=== handleManualBid() triggered for auction {} ===", auctionId);
        try {
            Auction auction = findAuction(auctionId);
            triggerAutoBidCompetition(auction);
            log.warn("Auto-bid competition completed for auction {}", auctionId);
        } catch (Exception e) {
            log.error("Error in handleManualBid for auction {}: {}", auctionId, e.getMessage());
            // Do not throw because this is an internal trigger
        }
    }

    private void triggerAutoBidCompetition(Auction auction) {
        log.warn("=== START auto-bid competition for auction {} ===", auction.getAuctionId());

        int iteration = 0;
        boolean hasNewBid;

        do {
            hasNewBid = false;
            iteration++;

            log.warn("Iteration {} for auction {}", iteration, auction.getAuctionId());

            if (iteration > MAX_AUTO_BID_ITERATIONS) {
                log.warn("Auto-bid competition reached max iterations ({}) for auction: {}",
                        MAX_AUTO_BID_ITERATIONS, auction.getAuctionId());
                break;
            }

            // 1) Get current highest bid
            log.warn("1) Getting current highest bid...");
            Optional<Bid> currentHighestOpt = bidRepository
                    .findTopByAuction_AuctionIdOrderByBidAmountDesc(auction.getAuctionId());

            Bid currentHighestBid = currentHighestOpt.orElse(null);
            BigDecimal currentHighestAmount = currentHighestBid != null
                    ? currentHighestBid.getBidAmount()
                    : auction.getProduct().getStartPrice();

            log.warn("Current highest: {} (bidder: {}, isAuto: {})",
                    currentHighestAmount,
                    currentHighestBid != null ? currentHighestBid.getBidder().getUserId() : "none",
                    currentHighestBid != null ? currentHighestBid.getIsAuto() : false);

            // 2) Get latest auto-bid configs for each bidder
            log.warn("2) Getting latest auto-bid configs...");
            List<Bid> allAutoBidConfigs = bidRepository.findByAuction_AuctionIdAndIsAutoTrue(auction.getAuctionId());

            Map<Long, Bid> latestAutoByBidder = allAutoBidConfigs.stream()
                    .filter(bid -> bid.getMaxAutobidAmount() != null)
                    .collect(Collectors.toMap(
                            b -> b.getBidder().getUserId(),
                            Function.identity(),
                            BinaryOperator.maxBy(Comparator.comparing(Bid::getCreatedAt))));

            if (latestAutoByBidder.isEmpty()) {
                log.warn("No auto-bidders found, ending competition");
                break;
            }

            log.warn("Found {} auto-bidders", latestAutoByBidder.size());

            // 3) Sort by maxAuto desc
            List<Bid> autoBidders = latestAutoByBidder.values().stream()
                    .sorted(Comparator.comparing(Bid::getMaxAutobidAmount).reversed())
                    .toList();

            // 4) Find challenger
            log.warn("3) Finding challenger...");
            Optional<Bid> challengerOpt = autoBidders.stream()
                    .filter(challenger -> {
                        // Skip current highest bidder
                        if (currentHighestBid != null &&
                                challenger.getBidder().getUserId().equals(currentHighestBid.getBidder().getUserId())) {
                            log.warn("Skipping current highest bidder: {}", challenger.getBidder().getUserId());
                            return false;
                        }
                        // Only take bidder with max > current highest
                        boolean canChallenge = challenger.getMaxAutobidAmount().compareTo(currentHighestAmount) > 0;
                        log.warn("Bidder {}: max={}, canChallenge={}",
                                challenger.getBidder().getUserId(),
                                challenger.getMaxAutobidAmount(),
                                canChallenge);
                        return canChallenge;
                    })
                    .findFirst();

            if (challengerOpt.isEmpty()) {
                log.warn("No challenger found, ending competition");
                break;
            }

            Bid challengerConfig = challengerOpt.get();
            log.warn("Challenger found: user={}, max={}",
                    challengerConfig.getBidder().getUserId(), challengerConfig.getMaxAutobidAmount());

            // 5) Calculate proposed amount
            log.warn("4) Calculating proposed amount...");
            BigDecimal step = challengerConfig.getStepAutoBidAmount() != null
                    ? challengerConfig.getStepAutoBidAmount()
                    : new BigDecimal("1000");

            if (step.compareTo(BigDecimal.ZERO) <= 0) {
                step = new BigDecimal("1000");
            }

            BigDecimal proposed = currentHighestAmount.add(step);
            log.warn("Current: {}, Step: {}, Proposed: {}", currentHighestAmount, step, proposed);

            // Limit by challenger's max
            if (proposed.compareTo(challengerConfig.getMaxAutobidAmount()) > 0) {
                proposed = challengerConfig.getMaxAutobidAmount();
                log.warn("Limited to max: {}", proposed);
            }

            // Check if proposed > current highest
            if (proposed.compareTo(currentHighestAmount) <= 0) {
                log.warn("Proposed amount {} not greater than current {}", proposed, currentHighestAmount);
                break;
            }

            // 6) Check challenger's balance
            log.warn("5) Checking challenger balance...");
            String challengerBalanceError = validateBalanceForBidAmountSafe(
                    challengerConfig.getBidder().getUserId(), proposed, auction.getAuctionId());
            if (challengerBalanceError != null) {
                log.warn("Challenger {} has insufficient balance: {}",
                        challengerConfig.getBidder().getUserId(), challengerBalanceError);
                // Skip this challenger and continue
                continue;
            }
            log.warn("Challenger has sufficient balance: {}", proposed);

            // 7) Reset highest flags
            log.warn("6) Resetting highest flags...");
            try {
                resetHighestBidFlags(auction.getAuctionId());
                log.warn("Highest flags reset");
            } catch (Exception e) {
                log.error("Error resetting highest flags: {}", e.getMessage());
                break;
            }

            // 8) Create REAL bid from auto-bid
            log.warn("7) Creating real bid from auto-bid...");
            Bid realBid = createRealBidFromAutoConfig(
                    auction,
                    challengerConfig.getBidder(),
                    proposed,
                    challengerConfig
            );

            log.warn("Real bid created from auto-bid: user={}, amount={}",
                    realBid.getBidder().getUserId(), realBid.getBidAmount());

            // 9) Update auction
            log.warn("8) Updating auction...");
            auction.setHighestCurrentPrice(proposed);
            auctionRepository.save(auction);
            log.warn("Auction updated with new highest: {}", proposed);

            // 10) Publish event
            try {
                eventPublisher.publishAutoBidTriggeredEvent(auction);

                // NOTIFICATIONS
                String auctionTitle = auction.getProduct().getName();
                Double bidAmountDouble = proposed.doubleValue();
                Long bidderId = challengerConfig.getBidder().getUserId();

                // 1. Notify Bidder (Auto Bid Placed)
                notificationPublisher.publishBidPlacedNotification(bidderId, auctionTitle, bidAmountDouble, auction.getAuctionId());

                // 2. Notify Previous Highest Bidder (Outbid)
                if (currentHighestBid != null && !currentHighestBid.getBidder().getUserId().equals(bidderId)) {
                     notificationPublisher.publishOutbidNotification(currentHighestBid.getBidder().getUserId(), auctionTitle, bidAmountDouble, auction.getAuctionId());
                }

                // 3. Notify Seller (Highest Bid Changed)
                Long sellerId = auction.getProduct().getSeller().getUserId();
                 if (!sellerId.equals(bidderId)) {
                     notificationPublisher.publishHighestBidderChangedNotification(sellerId, auctionTitle, bidAmountDouble, auction.getAuctionId());
                }

                log.warn("Notifications sent for auto-bid");
            } catch (Exception e) {
                log.warn("Error publishing event/notification: {}", e.getMessage());
            }

            hasNewBid = true;
            log.warn("New bid placed, continuing competition...");

        } while (hasNewBid);

        log.warn("=== END auto-bid competition for auction {} ===", auction.getAuctionId());
    }

    /**
     * Create auto-bid config (only saves max, step info, does NOT create actual bid)
     */
    private Bid createAutoBidConfig(Auction auction, User bidder, BigDecimal currentHighest,
                                    BigDecimal maxAmount, BigDecimal stepAmount) {
        Bid bid = new Bid();
        bid.setAuction(auction);
        bid.setBidder(bidder);
        bid.setBidAmount(currentHighest);
        bid.setMaxAutobidAmount(maxAmount);
        bid.setStepAutoBidAmount(stepAmount);
        bid.setIsAuto(true);
        bid.setIsHighest(false);
        bid.setCreatedAt(LocalDateTime.now());

        return bidRepository.save(bid);
    }

    /**
     * Create real bid from auto-bid config
     */
    private Bid createRealBidFromAutoConfig(Auction auction, User bidder, BigDecimal bidAmount, Bid autoConfig) {
        Bid bid = new Bid();
        bid.setAuction(auction);
        bid.setBidder(bidder);
        bid.setBidAmount(bidAmount);
        bid.setMaxAutobidAmount(autoConfig.getMaxAutobidAmount());
        bid.setStepAutoBidAmount(autoConfig.getStepAutoBidAmount());
        bid.setIsAuto(true);
        bid.setIsHighest(true);
        bid.setCreatedAt(LocalDateTime.now());

        return bidRepository.save(bid);
    }

    /**
     * SAFE VERSION: Check balance - returns error message or null if OK
     */
    private String validateBalanceForBidAmountSafe(Long userId, BigDecimal bidAmount, Long currentAuctionId) {
        log.warn("=== VALIDATE BALANCE SAFE ===");

        try {
            // 1. Calculate current withdrawable balance
            BigDecimal withdrawable = transactionService.getWithdrawable(userId);
            log.warn("Withdrawable balance: {}", withdrawable);

            // 2. Check if user is current highest bidder in this auction
            Optional<Bid> currentHighestInThisAuction = bidRepository
                    .findTopByAuction_AuctionIdOrderByBidAmountDesc(currentAuctionId);

            boolean isCurrentHighestBidder = currentHighestInThisAuction.isPresent() &&
                    currentHighestInThisAuction.get().getBidder().getUserId().equals(userId);

            BigDecimal currentHighestAmount = isCurrentHighestBidder ?
                    currentHighestInThisAuction.get().getBidAmount() : BigDecimal.ZERO;

            log.warn("Is highest bidder in current auction {}: {} (amount: {})",
                    currentAuctionId, isCurrentHighestBidder, currentHighestAmount);

            // 3. Calculate available amount
            BigDecimal availableForNewBid;

            if (isCurrentHighestBidder) {
                // User IS current highest bidder: can replace existing bid
                availableForNewBid = withdrawable.add(currentHighestAmount);
                log.warn("Available (replacing current highest): {} + {} = {}",
                        withdrawable, currentHighestAmount, availableForNewBid);
            } else {
                // User is NOT highest bidder
                availableForNewBid = withdrawable;
                log.warn("Available (new bid, not highest): {}", availableForNewBid);
            }

            log.warn("Final available for new bid: {}", availableForNewBid);
            log.warn("Bid amount requested: {}", bidAmount);

            if (availableForNewBid.compareTo(bidAmount) < 0) {
                log.warn("INSUFFICIENT: Available {} < Required {}", availableForNewBid, bidAmount);

                if (isCurrentHighestBidder) {
                    return String.format("""
                    Insufficient credit for auto-bid.
                    • Withdrawable balance: %s VND
                    • Amount locked in this auction: %s VND
                    • Total available: %s VND
                    • Required: %s VND""",
                            withdrawable, currentHighestAmount, availableForNewBid, bidAmount);
                } else {
                    return String.format("""
                    Not enough credit for auto-bid.
                    • Available: %s VND
                    • Require: %s VND""",
                            availableForNewBid, bidAmount);
                }
            }

            log.warn("Balance validation passed");
            return null; // No error

        } catch (Exception e) {
            log.warn("Error in balance validation: {}", e.getMessage());
            return "System error checking balance for auto-bid.";
        }
    }
}