package vn.team9.auction_system.common.dto.account;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AccountTransactionRequest {
    private Long userId;
    private BigDecimal amount;
    private String type; // DEPOSIT, WITHDRAW, TRANSFER, RECEIVED
}
