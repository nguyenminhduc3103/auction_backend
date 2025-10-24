package vn.team9.auction_system.common.dto.user;

import vn.team9.auction_system.common.base.BaseResponse;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserResponse extends BaseResponse {
    private Long userId;
    private String username;
    private String email;
    private BigDecimal balance;
    private String role;
    private String status;
    private LocalDateTime createdAt;
}
