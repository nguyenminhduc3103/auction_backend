package vn.team9.auction_system.common.dto.admin;

import lombok.Data;

@Data
public class UserWarningLogRequest {
    private Long userId;               // người bị cảnh báo
    private Long transactionId;        // giao dịch liên quan
    private String type;               // SHIPPED_NOT_PAID
    private String status;             // VIOLATION
    private String description;        // FOUND NG BEHAVIOR hoặc ghi chú thêm
    private Integer violationCount;    // lần vi phạm
}
