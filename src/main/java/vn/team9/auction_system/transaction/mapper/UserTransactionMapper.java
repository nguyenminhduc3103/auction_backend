package vn.team9.auction_system.transaction.mapper;
import vn.team9.auction_system.common.dto.transaction.TransactionResponse;
import vn.team9.auction_system.transaction.model.AccountTransaction;
public class UserTransactionMapper {
    public static TransactionResponse toDto(AccountTransaction t) {
        TransactionResponse dto = new TransactionResponse();
        dto.setTransactionID(t.getTransactionId());
        dto.setType(t.getType());
        dto.setStatus(t.getStatus());
        dto.setCreatedAt(t.getCreatedAt());
        dto.setAmount(t.getAmount());
        return dto;
    }
}