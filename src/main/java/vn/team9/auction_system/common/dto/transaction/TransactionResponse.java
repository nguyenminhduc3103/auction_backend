package vn.team9.auction_system.common.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TransactionResponse {
    private Long transactionID;
    private String type;
    private String status;
    private LocalDateTime createdAt;
    private BigDecimal amount;
    
}
