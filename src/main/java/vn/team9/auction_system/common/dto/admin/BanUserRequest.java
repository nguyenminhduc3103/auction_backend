package vn.team9.auction_system.common.dto.admin;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BanUserRequest {
    private Long userId;
    private String reason;           // ban reason
    private LocalDateTime bannedUntil; // time to unban
}
