package vn.team9.auction_system.common.service;

import org.springframework.data.domain.Page;
import vn.team9.auction_system.common.dto.account.AccountTransactionRequest;
import vn.team9.auction_system.common.dto.account.AccountTransactionResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface IAccountTransactionService {
    AccountTransactionResponse deposit(AccountTransactionRequest request);
    AccountTransactionResponse withdraw(AccountTransactionRequest request);
    Page<AccountTransactionResponse> getTransactionsByUser(
            Long userId,
            String status,
            String type,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size
    );


    AccountTransactionResponse transferBetweenUsers(Long fromUserId, Long toUserId, BigDecimal amount);
}
