package vn.team9.auction_system.common.base;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Lớp cha cho tất cả Response DTO.
 * Chứa các thuộc tính phản hồi chung.
 */
@Data
public abstract class BaseResponse {
    private boolean success = true;
    private String message;
    private LocalDateTime timestamp = LocalDateTime.now();
}
