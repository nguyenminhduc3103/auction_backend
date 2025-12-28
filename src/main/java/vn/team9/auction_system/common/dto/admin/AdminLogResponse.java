package vn.team9.auction_system.common.dto.admin;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminLogResponse {
    private Long id;               // Log ID
    private Long adminId;          // ID of admin performing the action
    private String action;         // e.g. APPROVE_PRODUCT, BAN_USER, REFUND
    private String target;         // Target object affected (product, user, feedback...)
    private String description;    // Detailed description of the action
    private String ipAddress;      // Admin's IP address when performing action
    private LocalDateTime createdAt; // Log creation time
    private LocalDateTime updatedAt; // Log update time (for CRUD)
    private String status;         // Log status, e.g. ACTIVE, DELETED
}