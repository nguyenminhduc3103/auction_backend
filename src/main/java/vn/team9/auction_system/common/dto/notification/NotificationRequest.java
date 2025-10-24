package vn.team9.auction_system.common.dto.notification;

import lombok.Data;

@Data
public class NotificationRequest {
    private Long userId;
    private String message;
    private String type; // BID, AUCTION_END, PAYMENT_SUCCESS, etc.
}
