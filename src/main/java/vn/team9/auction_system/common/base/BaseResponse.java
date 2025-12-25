package vn.team9.auction_system.common.base;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Parent class for all Response DTOs.
 * Contains common response properties.
 */
@Data
public abstract class BaseResponse {
    private boolean success = true;
    private String message;
    private LocalDateTime timestamp = LocalDateTime.now();
}