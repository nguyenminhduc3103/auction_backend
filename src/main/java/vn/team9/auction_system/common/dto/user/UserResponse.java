package vn.team9.auction_system.common.dto.user;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long userId;
    private String fullName;
    private String username;
    private String email;
    private String phone;
    private String gender;
    private BigDecimal balance;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime verifiedAt;
    private String avatarUrl;
    private Long roleId;
    private String roleName;
    private String reason;           // ban reason
    private LocalDateTime bannedUntil;
}
