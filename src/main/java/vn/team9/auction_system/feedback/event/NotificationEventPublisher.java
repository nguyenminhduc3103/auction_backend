package vn.team9.auction_system.feedback.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.team9.auction_system.common.dto.notification.NotificationRequest;
import vn.team9.auction_system.feedback.service.NotificationService;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventPublisher {
    
    private final NotificationService notificationService;

    /**
     * Send AUCTION_PENDING_APPROVAL notification to admin
     */
    public void publishAuctionPendingApprovalNotification(Long adminId, String auctionTitle, Long auctionId, String sellerName) {
        NotificationRequest request = NotificationRequest.builder()
            .userId(adminId)
            .title("Cuộc đấu giá chờ duyệt")
            .message("Cuộc đấu giá \"" + auctionTitle + "\" từ " + sellerName + " cần được duyệt")
            .type("SYSTEM")
            .category("AUCTION_PENDING_APPROVAL")
            .priority("HIGH")
            .actionUrl("/admin/auctions/approval")
            .actionLabel("Duyệt cuộc đấu giá")
            .metadata(Map.of("auctionId", auctionId, "sellerName", sellerName))
            .build();
        
        notificationService.sendNotification(request);
    }

    /**
     * Send AUCTION_APPROVED notification to seller
     */
    public void publishAuctionApprovedNotification(Long userId, String auctionTitle, Long auctionId) {
        NotificationRequest request = NotificationRequest.builder()
            .userId(userId)
            .title("Cuộc đấu giá được duyệt")
            .message("Cuộc đấu giá \"" + auctionTitle + "\" của bạn đã được admin duyệt")
            .type("SYSTEM")
            .category("AUCTION_APPROVED")
            .priority("MEDIUM")
            .actionUrl("/auctions/" + auctionId)
            .actionLabel("Xem cuộc đấu giá")
            .metadata(createMetadata(auctionId, null))
            .build();
        
        notificationService.sendNotification(request);
    }

    /**
     * Send AUCTION_STARTED notification to seller
     */
    public void publishAuctionStartedNotification(Long userId, String auctionTitle, Long auctionId) {
        NotificationRequest request = NotificationRequest.builder()
            .userId(userId)
            .title("Phiên đấu giá bắt đầu")
            .message("Phiên đấu giá \"" + auctionTitle + "\" của bạn đã bắt đầu")
            .type("SYSTEM")
            .category("AUCTION_STARTED")
            .priority("MEDIUM")
            .actionUrl("/auction/" + auctionId)
            .actionLabel("Xem cuộc đấu giá")
            .metadata(createMetadata(auctionId, null))
            .build();
        
        notificationService.sendNotification(request);
    }

    /**
     * Send BID_PLACED notification
     */
    public void publishBidPlacedNotification(Long userId, String auctionTitle, Double bidAmount, Long auctionId) {
        NotificationRequest request = NotificationRequest.builder()
            .userId(userId)
            .title("Đặt giá thành công")
            .message("Bạn vừa đặt giá " + bidAmount + "đ cho cuộc đấu giá: " + auctionTitle)
            .type("BID")
            .category("BID_PLACED")
            .priority("MEDIUM")
            .actionUrl("/auction/" + auctionId)
            .actionLabel("Xem cuộc đấu giá")
            .metadata(createMetadata(auctionId, bidAmount))
            .build();
        
        notificationService.sendNotification(request);
    }

    /**
     * Send LEADING_BID notification to current highest bidder
     */
    public void publishHighestBidderNotification(Long userId, String auctionTitle, Double bidAmount, Long auctionId) {
        NotificationRequest request = NotificationRequest.builder()
            .userId(userId)
            .title("Bạn đang dẫn đầu")
            .message("Bạn đang dẫn đầu cuộc đấu giá \"" + auctionTitle + "\" với giá " + bidAmount + "đ")
            .type("BID")
            .category("LEADING_BID")
            .priority("MEDIUM")
            .actionUrl("/auction/" + auctionId)
            .actionLabel("Xem cuộc đấu giá")
            .metadata(createMetadata(auctionId, bidAmount))
            .build();
        
        notificationService.sendNotification(request);
    }

    /**
     * Send OUTBID notification (to previous highest bidder)
     */
    public void publishOutbidNotification(Long userId, String auctionTitle, Double newBidAmount, Long auctionId) {
        NotificationRequest request = NotificationRequest.builder()
            .userId(userId)
            .title("Bị đánh giá tốt hơn")
            .message("Ai đó vừa đặt giá " + newBidAmount + "đ cao hơn giá của bạn trong cuộc đấu giá: " + auctionTitle)
            .type("BID")
            .category("OUTBID")
            .priority("HIGH")
            .actionUrl("/auction/" + auctionId)
            .actionLabel("Đặt giá tiếp")
            .metadata(createMetadata(auctionId, newBidAmount))
            .build();
        
        notificationService.sendNotification(request);
    }

    /**
     * Send HIGHEST_BID_CHANGED notification to seller
     */
    public void publishHighestBidderChangedNotification(Long userId, String auctionTitle, Double highestBidAmount, Long auctionId) {
        NotificationRequest request = NotificationRequest.builder()
            .userId(userId)
            .title("Giá cao nhất thay đổi")
            .message("Cuộc đấu giá \"" + auctionTitle + "\" có người đặt giá cao nhất mới: " + highestBidAmount + "đ")
            .type("BID")
            .category("HIGHEST_BID_CHANGED")
            .priority("MEDIUM")
            .actionUrl("/auction/" + auctionId)
            .actionLabel("Xem chi tiết")
            .metadata(createMetadata(auctionId, highestBidAmount))
            .build();
        
        notificationService.sendNotification(request);
    }

    /**
     * Send AUCTION_ENDING_SOON notification
     */
    public void publishAuctionEndingSoonNotification(Long userId, String auctionTitle, Long auctionId) {
        NotificationRequest request = NotificationRequest.builder()
            .userId(userId)
            .title("Cuộc đấu giá sắp kết thúc")
            .message("Cuộc đấu giá \"" + auctionTitle + "\" sẽ kết thúc trong 5 phút nữa")
            .type("BID")
            .category("AUCTION_ENDING_SOON")
            .priority("HIGH")
            .actionUrl("/auction/" + auctionId)
            .actionLabel("Xem ngay")
            .metadata(createMetadata(auctionId, null))
            .build();
        
        notificationService.sendNotification(request);
    }

    /**
     * Send AUCTION_WON notification
     */
    public void publishAuctionWonNotification(Long userId, String auctionTitle, Double finalPrice, Long auctionId) {
        NotificationRequest request = NotificationRequest.builder()
            .userId(userId)
            .title("🎉 Chúc mừng bạn đã thắng")
            .message("Bạn đã thắng cuộc đấu giá \"" + auctionTitle + "\" với giá " + finalPrice + "đ")
            .type("BID")
            .category("AUCTION_WON")
            .priority("HIGH")
            .actionUrl("/auctions/" + auctionId)
            .actionLabel("Thanh toán ngay")
            .metadata(Map.of(
                "auctionId", auctionId,
                "finalPrice", finalPrice
            ))
            .build();
        
        notificationService.sendNotification(request);
    }

    /**
     * Send AUCTION_LOST notification
     */
    public void publishAuctionLostNotification(Long userId, String auctionTitle, Long auctionId) {
        NotificationRequest request = NotificationRequest.builder()
            .userId(userId)
            .title("Cuộc đấu giá kết thúc")
            .message("Bạn không thắng cuộc đấu giá \"" + auctionTitle + "\"")
            .type("BID")
            .category("AUCTION_LOST")
            .priority("LOW")
            .actionUrl("/auction/" + auctionId)
            .actionLabel("Xem chi tiết")
            .metadata(createMetadata(auctionId, null))
            .build();
        
        notificationService.sendNotification(request);
    }

    /**
     * Send PAYMENT_DUE notification
     */
    public void publishPaymentDueNotification(Long userId, Double amount, Long transactionId) {
        NotificationRequest request = NotificationRequest.builder()
            .userId(userId)
            .title("Cần thanh toán")
            .message("Bạn cần thanh toán " + amount + "đ cho đơn hàng của bạn")
            .type("PAYMENT")
            .category("PAYMENT_DUE")
            .priority("HIGH")
            .actionUrl("/user/account/payment")
            .actionLabel("Thanh toán")
            .metadata(Map.of(
                "transactionId", transactionId,
                "amount", amount
            ))
            .build();
        
        notificationService.sendNotification(request);
    }

    /**
     * Send PAYMENT_SUCCESS notification
     */
    public void publishPaymentSuccessNotification(Long userId, Double amount, Long transactionId) {
        NotificationRequest request = NotificationRequest.builder()
            .userId(userId)
            .title("Thanh toán thành công")
            .message("Bạn đã thanh toán thành công " + amount + "đ")
            .type("PAYMENT")
            .category("PAYMENT_SUCCESS")
            .priority("MEDIUM")
            .actionUrl("/user/account/payment")
            .actionLabel("Chi tiết")
            .metadata(Map.of(
                "transactionId", transactionId,
                "amount", amount
            ))
            .build();
        
        notificationService.sendNotification(request);
    }

    /**
     * Send PAYMENT_FAILED notification
     */
    public void publishPaymentFailedNotification(Long userId, Double amount, String reason, Long transactionId) {
        NotificationRequest request = NotificationRequest.builder()
            .userId(userId)
            .title("Thanh toán thất bại")
            .message("Lỗi thanh toán " + amount + "đ: " + reason)
            .type("PAYMENT")
            .category("PAYMENT_FAILED")
            .priority("HIGH")
            .actionUrl("/user/account/payment")
            .actionLabel("Thử lại")
            .metadata(Map.of(
                "transactionId", transactionId,
                "amount", amount,
                "reason", reason
            ))
            .build();
        
        notificationService.sendNotification(request);
    }

    /**
     * Send PAYMENT_CONFIRMED notification to seller
     */
    public void publishPaymentConfirmedNotification(Long sellerId, String buyerName, Double amount, Long transactionId) {
        NotificationRequest request = NotificationRequest.builder()
            .userId(sellerId)
            .title("✅ Thanh toán đã được xác nhận")
            .message("Người mua " + buyerName + " đã thanh toán thành công " + amount + "đ")
            .type("PAYMENT")
            .category("PAYMENT_CONFIRMED")
            .priority("HIGH")
            .actionUrl("/seller/auctions")
            .actionLabel("Xem đơn hàng")
            .metadata(Map.of(
                "transactionId", transactionId,
                "amount", amount,
                "buyerName", buyerName
            ))
            .build();
        
        notificationService.sendNotification(request);
    }

    /**
     * Send PAYMENT_PENDING notification to seller
     */
    public void publishPaymentPendingNotification(Long sellerId, String buyerName, Double amount, Long transactionId) {
        NotificationRequest request = NotificationRequest.builder()
            .userId(sellerId)
            .title("⏳ Chờ thanh toán từ người mua")
            .message("Người mua " + buyerName + " chưa thanh toán cho đơn hàng " + amount + "đ")
            .type("PAYMENT")
            .category("PAYMENT_PENDING")
            .priority("MEDIUM")
            .actionUrl("/seller/auctions")
            .actionLabel("Xem chi tiết")
            .metadata(Map.of(
                "transactionId", transactionId,
                "amount", amount,
                "buyerName", buyerName
            ))
            .build();
        
        notificationService.sendNotification(request);
    }

    /**
     * Send SHIPMENT_CONFIRMED notification
     */
    public void publishShipmentConfirmedNotification(Long userId, String productName, String trackingNumber) {
        NotificationRequest request = NotificationRequest.builder()
            .userId(userId)
            .title("Đơn hàng đã được gửi")
            .message("Đơn hàng \"" + productName + "\" đã được gửi đi. Mã theo dõi: " + trackingNumber)
            .type("PAYMENT")
            .category("SHIPMENT_CONFIRMED")
            .priority("MEDIUM")
            .actionUrl("/orders")
            .actionLabel("Theo dõi đơn hàng")
            .metadata(Map.of(
                "trackingNumber", trackingNumber,
                "productName", productName
            ))
            .build();
        
        notificationService.sendNotification(request);
    }

    /**
     * Send SYSTEM ANNOUNCEMENT notification to multiple users
     */
    public void publishSystemAnnouncement(java.util.List<Long> userIds, String announcement) {
        NotificationRequest request = NotificationRequest.builder()
            .title("Thông báo hệ thống")
            .message(announcement)
            .type("SYSTEM")
            .category("ANNOUNCEMENT")
            .priority("MEDIUM")
            .build();
        
        notificationService.sendSystemNotificationToUsers(userIds, request);
    }

    /**
     * Helper method to create metadata map
     */
    private Map<String, Object> createMetadata(Long auctionId, Double amount) {
        Map<String, Object> metadata = new HashMap<>();
        if (auctionId != null) {
            metadata.put("auctionId", auctionId);
        }
        if (amount != null) {
            metadata.put("amount", amount);
        }
        metadata.put("timestamp", System.currentTimeMillis());
        return metadata;
    }
}
