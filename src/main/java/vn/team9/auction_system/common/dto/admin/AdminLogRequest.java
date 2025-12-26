package vn.team9.auction_system.common.dto.admin;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminLogRequest {
    private Long id;              // Log ID (used for update/delete)
    private Long adminId;         // ID of admin performing the action
    private String action;        // e.g. APPROVE_PRODUCT, BAN_USER, REFUND
    private String target;        // Target object affected (product, user, feedback...)
    private String description;   // Detailed description of the action

    // Extended CRUD fields
    private LocalDateTime createdAt; // Log creation time
    private LocalDateTime updatedAt; // Log update time
}