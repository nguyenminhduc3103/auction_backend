package vn.team9.auction_system.auction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.team9.auction_system.auction.model.Auction;
import vn.team9.auction_system.auction.model.Bid;
import vn.team9.auction_system.auction.repository.BidRepository;
import vn.team9.auction_system.common.dto.notification.NotificationRequest;
import vn.team9.auction_system.common.service.INotificationService;
import vn.team9.auction_system.user.model.User;
import vn.team9.auction_system.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuctionNotificationService {

    private final INotificationService notificationService;
    private final BidRepository bidRepository;
    private final UserRepository userRepository;

    /**
     * Gửi thông báo yêu cầu xét duyệt đến tất cả Admin
     * Khi seller tạo auction request
     */
    public void notifyAdminAuctionPendingReview(Auction auction) {
        try {
            // Get all admin users from database
            List<User> adminUsers = userRepository.findByRole_RoleName("ADMIN");

            if (adminUsers.isEmpty()) {
                log.warn("No admin users found to send notification for auction: {}", auction.getAuctionId());
                return;
            }

            String message = String.format(
                    "Seller '%s' đã gửi yêu cầu xét duyệt sản phẩm '%s' cho đấu giá. " +
                            "Thời gian: %s - %s",
                    auction.getProduct().getSeller().getFullName(),
                    auction.getProduct().getName(),
                    auction.getStartTime(),
                    auction.getEndTime());

            // Send notification to each admin
            for (User admin : adminUsers) {
                try {
                    NotificationRequest request = NotificationRequest.builder()
                            .userId(admin.getUserId())
                            .title("Xét duyệt đấu giá mới")
                            .message(message)
                            .type("SYSTEM")
                            .category("AUCTION_PENDING_APPROVAL")
                            .priority("HIGH")
                            .actionUrl("/admin/auctions/approval")
                            .actionLabel("Xem chi tiết")
                            .build();

                    notificationService.sendNotification(request);
                    log.info("Sent auction pending review notification to admin: {}", admin.getUserId());
                } catch (Exception innerEx) {
                    log.error("Failed to send notification to admin: {}", admin.getUserId(), innerEx);
                }
            }

            log.info("Sent auction pending review notification to {} admins for auction: {}",
                    adminUsers.size(), auction.getAuctionId());
        } catch (Exception e) {
            log.error("Failed to send auction pending review notification", e);
        }
    }

    /**
     * Gửi thông báo auction đã được duyệt đến Seller
     */
    public void notifySellerAuctionApproved(Auction auction) {
        try {
            User seller = auction.getProduct().getSeller();
            String message = String.format(
                    "Yêu cầu đấu giá sản phẩm '%s' của bạn đã được Admin duyệt. " +
                            "Phiên đấu giá sẽ bắt đầu vào lúc %s",
                    auction.getProduct().getName(),
                    auction.getStartTime());

            NotificationRequest request = NotificationRequest.builder()
                    .userId(seller.getUserId())
                    .title("✅ Đấu giá được duyệt")
                    .message(message)
                    .type("BID")
                    .category("AUCTION_APPROVED")
                    .priority("HIGH")
                    .actionUrl("auctions/" + auction.getAuctionId())
                    .actionLabel("Xem chi tiết")
                    .build();

            notificationService.sendNotification(request);
            log.info("Sent auction approved notification to seller: {}", seller.getUserId());
        } catch (Exception e) {
            log.error("Failed to send auction approved notification", e);
        }
    }

    /**
     * Gửi thông báo auction đã bị từ chối đến Seller
     */
    public void notifySellerAuctionRejected(Auction auction, String rejectionReason) {
        try {
            User seller = auction.getProduct().getSeller();
            String message = String.format(
                    "Yêu cầu đấu giá sản phẩm '%s' của bạn đã bị từ chối.\nLý do: %s",
                    auction.getProduct().getName(),
                    rejectionReason != null ? rejectionReason : "Không có lý do");

            NotificationRequest request = NotificationRequest.builder()
                    .userId(seller.getUserId())
                    .title("❌ Đấu giá bị từ chối")
                    .message(message)
                    .type("BID")
                    .category("AUCTION_REJECTED")
                    .priority("HIGH")
                    .actionUrl("/seller/auctions/" + auction.getAuctionId())
                    .actionLabel("Xem chi tiết")
                    .build();

            notificationService.sendNotification(request);
            log.info("Sent auction rejected notification to seller: {}", seller.getUserId());
        } catch (Exception e) {
            log.error("Failed to send auction rejected notification", e);
        }
    }

    /**
     * Gửi thông báo phiên đấu giá đã bắt đầu đến Seller
     */
    public void notifySellerAuctionStarted(Auction auction) {
        try {
            User seller = auction.getProduct().getSeller();
            String message = String.format(
                    "Phiên đấu giá sản phẩm '%s' của bạn đã chính thức bắt đầu!\\n" +
                            "Thời gian kết thúc: %s",
                    auction.getProduct().getName(),
                    auction.getEndTime());

            NotificationRequest request = NotificationRequest.builder()
                    .userId(seller.getUserId())
                    .title("🔨 Phiên đấu giá bắt đầu")
                    .message(message)
                    .type("BID")
                    .category("AUCTION_STARTED")
                    .priority("MEDIUM")
                    .actionUrl("/seller/auctions/" + auction.getAuctionId())
                    .actionLabel("Xem phiên đấu giá")
                    .build();

            notificationService.sendNotification(request);
            log.info("Sent auction started notification to seller: {}", seller.getUserId());
        } catch (Exception e) {
            log.error("Failed to send auction started notification", e);
        }
    }

    /**
     * Gửi thông báo bidder đã đặt giá thành công
     */
    public void notifyBidderBidPlaced(Bid bid) {
        try {
            User bidder = bid.getBidder();
            Auction auction = bid.getAuction();
            String message = String.format(
                    "✅ Bạn đã tham gia thành công sản phẩm '%s' với giá ₫%s",
                    auction.getProduct().getName(),
                    bid.getBidAmount().toPlainString());

            NotificationRequest request = NotificationRequest.builder()
                    .userId(bidder.getUserId())
                    .title("✅ Đặt giá thành công")
                    .message(message)
                    .type("BID")
                    .category("BID_PLACED")
                    .priority("MEDIUM")
                    .actionUrl("/auction/" + auction.getAuctionId())
                    .actionLabel("Xem phiên đấu giá")
                    .build();

            notificationService.sendNotification(request);
            log.info("Sent bid placed notification to bidder: {}", bidder.getUserId());
        } catch (Exception e) {
            log.error("Failed to send bid placed notification", e);
        }
    }

    /**
     * Gửi thông báo bidder đang dẫn đầu
     */
    public void notifyBidderLeadingBid(Bid bid) {
        try {
            User bidder = bid.getBidder();
            Auction auction = bid.getAuction();
            String message = String.format(
                    "🏆 Bạn đang dẫn đầu với giá ₫%s cho sản phẩm '%s'",
                    bid.getBidAmount().toPlainString(),
                    auction.getProduct().getName());

            NotificationRequest request = NotificationRequest.builder()
                    .userId(bidder.getUserId())
                    .title("🏆 Bạn đang dẫn đầu")
                    .message(message)
                    .type("BID")
                    .category("LEADING_BID")
                    .priority("MEDIUM")
                    .actionUrl("/auction/" + auction.getAuctionId())
                    .actionLabel("Xem chi tiết")
                    .build();

            notificationService.sendNotification(request);
            log.info("Sent leading bid notification to bidder: {}", bidder.getUserId());
        } catch (Exception e) {
            log.error("Failed to send leading bid notification", e);
        }
    }

    /**
     * Gửi thông báo bidder đã bị out top
     */
    public void notifyBidderOutbid(Bid previousHighestBid, Bid newHighestBid) {
        try {
            User previousBidder = previousHighestBid.getBidder();
            Auction auction = previousHighestBid.getAuction();
            String message = String.format(
                    "⚠️ Bạn đã bị vượt qua! Giá hiện tại là ₫%s cho sản phẩm '%s'",
                    newHighestBid.getBidAmount().toPlainString(),
                    auction.getProduct().getName());

            NotificationRequest request = NotificationRequest.builder()
                    .userId(previousBidder.getUserId())
                    .title("⚠️ Bạn đã bị vượt qua")
                    .message(message)
                    .type("BID")
                    .category("OUTBID")
                    .priority("HIGH")
                    .actionUrl("/auction/" + auction.getAuctionId())
                    .actionLabel("Đặt giá cao hơn")
                    .build();

            notificationService.sendNotification(request);
            log.info("Sent outbid notification to bidder: {}", previousBidder.getUserId());
        } catch (Exception e) {
            log.error("Failed to send outbid notification", e);
        }
    }

    /**
     * Gửi thông báo giá cao nhất thay đổi đến Seller
     */
    public void notifySellerHighestBidChanged(Auction auction, Bid newHighestBid) {
        try {
            User seller = auction.getProduct().getSeller();
            String message = String.format(
                    "💰 Giá cao nhất vừa được cập nhật: ₫%s cho sản phẩm '%s'",
                    newHighestBid.getBidAmount().toPlainString(),
                    auction.getProduct().getName());

            NotificationRequest request = NotificationRequest.builder()
                    .userId(seller.getUserId())
                    .title("💰 Giá cao nhất thay đổi")
                    .message(message)
                    .type("BID")
                    .category("HIGHEST_BID_CHANGED")
                    .priority("MEDIUM")
                    .actionUrl("/auctions/" + auction.getAuctionId())
                    .actionLabel("Xem chi tiết")
                    .build();

            notificationService.sendNotification(request);
            log.info("Sent highest bid changed notification to seller: {}", seller.getUserId());
        } catch (Exception e) {
            log.error("Failed to send highest bid changed notification", e);
        }
    }

    /**
     * Gửi thông báo phiên đấu giá kết thúc đến Seller và tất cả Bidders
     */
    public void notifyAuctionEnded(Auction auction) {
        try {
            // Notify seller
            notifySellerAuctionEnded(auction);

            // Notify all bidders
            notifyAllBiddersAuctionEnded(auction);

            // Notify winner separately
            if (auction.getWinner() != null) {
                notifyBidderWon(auction);
            }
        } catch (Exception e) {
            log.error("Failed to notify auction ended", e);
        }
    }

    /**
     * Gửi thông báo kết thúc đến Seller
     */
    private void notifySellerAuctionEnded(Auction auction) {
        try {
            User seller = auction.getProduct().getSeller();
            String winnerInfo = auction.getWinner() != null
                    ? String.format("Người thắng: %s", auction.getWinner().getFullName())
                    : "Không có người thắng";

            String message = String.format(
                    "⏱️ Phiên đấu giá sản phẩm '%s' đã kết thúc!\n" +
                            "Giá cao nhất: ₫%s\n%s",
                    auction.getProduct().getName(),
                    auction.getHighestCurrentPrice().toPlainString(),
                    winnerInfo);

            NotificationRequest request = NotificationRequest.builder()
                    .userId(seller.getUserId())
                    .title("⏱️ Phiên đấu giá kết thúc")
                    .message(message)
                    .type("BID")
                    .category("AUCTION_ENDED")
                    .priority("HIGH")
                    .actionUrl("/seller/auctions/" + auction.getAuctionId())
                    .actionLabel("Xem kết quả")
                    .build();

            notificationService.sendNotification(request);
            log.info("Sent auction ended notification to seller: {}", seller.getUserId());
        } catch (Exception e) {
            log.error("Failed to send seller auction ended notification", e);
        }
    }

    /**
     * Gửi thông báo kết thúc đến tất cả Bidders
     */
    private void notifyAllBiddersAuctionEnded(Auction auction) {
        try {
            List<Bid> allBids = bidRepository.findByAuction_AuctionId(auction.getAuctionId());
            List<Long> bidderIds = allBids.stream()
                    .map(bid -> bid.getBidder().getUserId())
                    .distinct()
                    .collect(Collectors.toList());

            for (Long bidderId : bidderIds) {
                if (auction.getWinner() == null || !auction.getWinner().getUserId().equals(bidderId)) {
                    // Notify losing bidders
                    notifyBidderLost(auction, bidderId);
                }
            }
        } catch (Exception e) {
            log.error("Failed to notify all bidders auction ended", e);
        }
    }

    /**
     * Gửi thông báo người thắng đấu giá
     */
    private void notifyBidderWon(Auction auction) {
        try {
            User winner = auction.getWinner();
            String message = String.format(
                    "🎉 Chúc mừng! Bạn đã thắng sản phẩm '%s' với giá ₫%s\n" +
                            "Vui lòng tiến hành thanh toán trong 24 giờ",
                    auction.getProduct().getName(),
                    auction.getHighestCurrentPrice().toPlainString());

            NotificationRequest request = NotificationRequest.builder()
                    .userId(winner.getUserId())
                    .title("🎉 Bạn đã thắng!")
                    .message(message)
                    .type("BID")
                    .category("AUCTION_WON")
                    .priority("HIGH")
                    .actionUrl("/auction/" + auction.getAuctionId())
                    .actionLabel("Thanh toán ngay")
                    .build();

            notificationService.sendNotification(request);
            log.info("Sent auction won notification to winner: {}", winner.getUserId());
        } catch (Exception e) {
            log.error("Failed to send auction won notification", e);
        }
    }

    /**
     * Gửi thông báo người thua đấu giá
     */
    private void notifyBidderLost(Auction auction, Long bidderId) {
        try {
            String message = String.format(
                    "Phiên đấu giá sản phẩm '%s' đã kết thúc.\\n" +
                            "Giá cao nhất: ₫%s",
                    auction.getProduct().getName(),
                    auction.getHighestCurrentPrice().toPlainString());

            NotificationRequest request = NotificationRequest.builder()
                    .userId(bidderId)
                    .title("Phiên đấu giá kết thúc")
                    .message(message)
                    .type("BID")
                    .category("AUCTION_LOST")
                    .priority("LOW")
                    .actionUrl("/auction/" + auction.getAuctionId())
                    .actionLabel("Xem chi tiết")
                    .build();

            notificationService.sendNotification(request);
            log.info("Sent auction lost notification to bidder: {}", bidderId);
        } catch (Exception e) {
            log.error("Failed to send auction lost notification", e);
        }
    }
}
