package vn.team9.auction_system.auction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.team9.auction_system.auction.model.Auction;
import vn.team9.auction_system.auction.repository.AuctionRepository;
import vn.team9.auction_system.common.service.IAuctionService;
import vn.team9.auction_system.feedback.event.NotificationEventPublisher;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionScheduler {

    private final AuctionRepository auctionRepository;
    private final IAuctionService auctionService;
    private final NotificationEventPublisher notificationPublisher;

    // Chạy mỗi 10 giây
    @Scheduled(cron = "*/10 * * * * *")
    @Transactional
    public void checkAuctions() {
        LocalDateTime now = LocalDateTime.now();

        List<Auction> auctions = auctionRepository.findAll();

        for (Auction auction : auctions) {
            try {
                // Auto-start PENDING auction
                if ("PENDING".equals(auction.getStatus()) && auction.getStartTime().isBefore(now)) {
                    auctionService.startAuction(auction.getAuctionId());
                    log.info("Started auction: {}", auction.getAuctionId());
                }

                // 🆕 Check if auction ending soon (5 minutes left)
                if ("OPEN".equals(auction.getStatus())) {
                    LocalDateTime endTime = auction.getEndTime();
                    if (endTime != null) {
                        long minutesLeft = ChronoUnit.MINUTES.between(now, endTime);

                        // Notify when 5 minutes or less left (>= 5 and < 6)
                        if (minutesLeft >= 5 && minutesLeft < 6) {
                            try {
                                // Notify all bidders who have placed bids
                                if (auction.getBids() != null && !auction.getBids().isEmpty()) {
                                    auction.getBids().stream()
                                            .map(bid -> bid.getBidder().getUserId())
                                            .distinct()
                                            .forEach(userId -> {
                                                try {
                                                    // ✅ FIX: Check if notification already sent to avoid duplicates
                                                    boolean alreadySent = notificationPublisher.hasNotificationBeenSent(
                                                            userId,
                                                            "AUCTION_ENDING_SOON",
                                                            auction.getAuctionId());

                                                    if (!alreadySent) {
                                                        notificationPublisher.publishAuctionEndingSoonNotification(
                                                                userId,
                                                                auction.getProduct().getName(),
                                                                auction.getAuctionId());
                                                        log.info("✅ Sent AUCTION_ENDING_SOON to user: {}", userId);
                                                    } else {
                                                        log.debug(
                                                                "⏭️ Skipped duplicate AUCTION_ENDING_SOON for user: {}",
                                                                userId);
                                                    }
                                                } catch (Exception e) {
                                                    log.warn("Failed to send ending soon notification to user {}: {}",
                                                            userId, e.getMessage());
                                                }
                                            });
                                    log.info("✅ AUCTION_ENDING_SOON notifications processed for auction: {}",
                                            auction.getAuctionId());
                                }
                            } catch (Exception e) {
                                log.warn("Error sending ending soon notifications: {}", e.getMessage());
                            }
                        }
                    }
                }

                // Auto-close OPEN auction
                if ("OPEN".equals(auction.getStatus()) && auction.getEndTime().isBefore(now)) {
                    auctionService.closeAuction(auction.getAuctionId());
                    log.info("Closed auction: {}", auction.getAuctionId());
                }
            } catch (Exception e) {
                log.error("Error processing auction {}: {}", auction.getAuctionId(), e.getMessage());
            }
        }
    }
}
