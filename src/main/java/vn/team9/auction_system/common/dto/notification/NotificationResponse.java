package vn.team9.auction_system.common.dto.notification;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long id;
    private Long userId;
    private String message;
    private String type;
    private boolean isRead;
    private LocalDateTime createdAt;
}
