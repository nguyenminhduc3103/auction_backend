package vn.team9.auction_system.common.dto.admin;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminLogRequest {
    private Long id;              // 🆕 ID log (dùng cho update/delete)
    private Long adminId;         // ID của admin thực hiện hành động
    private String action;        // e.g. APPROVE_PRODUCT, BAN_USER, REFUND
    private String target;        // Đối tượng bị tác động (product, user, feedback...)
    private String description;   // Mô tả chi tiết hành động

    // 🕓 Các trường CRUD mở rộng
    private LocalDateTime createdAt; // Thời gian tạo log
    private LocalDateTime updatedAt; // Thời gian cập nhật log
}
