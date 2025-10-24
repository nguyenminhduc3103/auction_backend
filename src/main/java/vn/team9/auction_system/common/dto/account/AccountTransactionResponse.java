package vn.team9.auction_system.common.dto.account;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountTransactionResponse {
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private String type;
    private String status;
    private LocalDateTime createdAt;
}
