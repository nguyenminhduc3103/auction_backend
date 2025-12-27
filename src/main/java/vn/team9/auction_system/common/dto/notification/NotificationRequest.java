package vn.team9.auction_system.common.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {
    @NotNull(message = "userId cannot be null")
    private Long userId;

    @NotBlank(message = "title cannot be blank")
    @Size(min = 1, max = 255, message = "title must be between 1 and 255 characters")
    private String title;

    @NotBlank(message = "message cannot be blank")
    @Size(min = 1, max = 5000, message = "message must be between 1 and 5000 characters")
    private String message;

    @NotBlank(message = "type cannot be blank")
    private String type; // SYSTEM, BID, PAYMENT

    @NotBlank(message = "category cannot be blank")
    @Size(min = 1, max = 50, message = "category must be between 1 and 50 characters")
    private String category;

    private String priority; // LOW, MEDIUM, HIGH

    @Size(max = 500, message = "actionUrl must not exceed 500 characters")
    private String actionUrl;

    @Size(max = 100, message = "actionLabel must not exceed 100 characters")
    private String actionLabel;

    private Map<String, Object> metadata;
}
