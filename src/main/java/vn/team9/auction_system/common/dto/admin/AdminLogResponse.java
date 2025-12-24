package vn.team9.auction_system.common.dto.admin;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminLogResponse {
    private Long id;               // ID log
    private Long adminId;          // ID của admin thực hiện hành động
    private String action;         // e.g. APPROVE_PRODUCT, BAN_USER, REFUND
    private String target;         // Đối tượng bị tác động (product, user, feedback...)
    private String description;    // Mô tả chi tiết hành động
    private String ipAddress;      // IP của admin khi thực hiện
    private LocalDateTime createdAt; // Thời gian tạo log
    private LocalDateTime updatedAt; // 🆕 Thời gian cập nhật log (cho CRUD)
    private String status;           // 🆕 Trạng thái log, ví dụ: ACTIVE, DELETED
}
