package vn.team9.auction_system.common.base;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Parent class for all Request DTOs.
 * Helps standardize common properties when sending requests from client to server.
 */
@Data
public abstract class BaseRequest {
    private String requestId;
    private LocalDateTime timestamp = LocalDateTime.now();
}