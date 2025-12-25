package vn.team9.auction_system.common.dto.admin;

import lombok.Data;

@Data
public class UserWarningLogRequest {
    private Long userId;               // User who received warning
    private Long transactionId;        // Related transaction
    private String type;               // SHIPPED_NOT_PAID
    private String status;             // VIOLATION
    private String description;        // FOUND SUSPICIOUS BEHAVIOR or additional notes
    private Integer violationCount;    // Number of violations
}