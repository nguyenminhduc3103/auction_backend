package vn.team9.auction_system.common.dto.admin;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminLogResponse {
    private Long id;
    private Long adminId;
    private String action;
    private String target;
    private String description;
    private String ipAddress;
    private LocalDateTime createdAt;
}
