package vn.team9.auction_system.common.base;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Lớp cha cho tất cả Request DTO.
 * Giúp chuẩn hoá các thuộc tính chung khi gửi request từ client lên.
 */
@Data
public abstract class BaseRequest {
    private String requestId;
    private LocalDateTime timestamp = LocalDateTime.now();
}