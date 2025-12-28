package vn.team9.auction_system.common.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long notiId;
    private Long userId;
    private String title;
    private String message;
    private String type;
    private String category;
    private String priority;
    private Boolean isRead;
    private LocalDateTime readAt;
    private String actionUrl;
    private String actionLabel;
    private JsonNode metadata;
    private LocalDateTime createdAt;
}
