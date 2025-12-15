package vn.team9.auction_system.common.dto.admin;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserWarningLogResponse {
    private Long logId;
    private Long userId;
    private Long transactionId;
    private String type;
    private String status;
    private String description;
    private Integer violationCount;
    private LocalDateTime createdAt;
}
