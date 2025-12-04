package vn.team9.auction_system.transaction.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.team9.auction_system.common.dto.account.AccountTransactionRequest;
import vn.team9.auction_system.common.dto.account.AccountTransactionResponse;
import vn.team9.auction_system.common.service.IAccountTransactionService;
import vn.team9.auction_system.transaction.model.AccountTransaction;
import vn.team9.auction_system.transaction.repository.AccountTransactionRepository;
import vn.team9.auction_system.user.model.User;
import vn.team9.auction_system.user.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountTransactionServiceImpl implements IAccountTransactionService {

    private final AccountTransactionRepository accountTransactionRepository;
    private final UserRepository userRepository;

    // ===========================
    // DEPOSIT
    // ===========================
    @Transactional
    @Override
    public AccountTransactionResponse deposit(AccountTransactionRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // + balance ngay
        user.setBalance(user.getBalance().add(request.getAmount()));
        userRepository.save(user);

        // tạo transaction
        AccountTransaction tx = new AccountTransaction();
        tx.setUser(user);
        tx.setAmount(request.getAmount());
        tx.setType("DEPOSIT");
        tx.setStatus("SUCCESS");
        tx.setCreatedAt(LocalDateTime.now());

        accountTransactionRepository.save(tx);

        return toResponse(tx);
    }

    // ===========================
    // WITHDRAW (PENDING)
    // ===========================
    @Transactional
    @Override
    public AccountTransactionResponse withdraw(AccountTransactionRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check đủ tiền (không có lockedBalance)
        if (user.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Không đủ tiền để rút");
        }

        // Không trừ tiền ngay → tạo PENDING
        AccountTransaction tx = new AccountTransaction();
        tx.setUser(user);
        tx.setAmount(request.getAmount());
        tx.setType("WITHDRAW");
        tx.setStatus("PENDING");
        tx.setCreatedAt(LocalDateTime.now());

        accountTransactionRepository.save(tx);

        return toResponse(tx);
    }

    // ===========================
    // CONFIRM WITHDRAW (ADMIN)
    // ===========================
    @Transactional
    public AccountTransactionResponse confirmWithdraw(Long transactionId) {

        AccountTransaction tx = accountTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!"PENDING".equals(tx.getStatus())) {
            throw new RuntimeException("Withdraw không ở trạng thái PENDING");
        }

        User user = tx.getUser();

        // Lúc confirm mới trừ tiền
        if (user.getBalance().compareTo(tx.getAmount()) < 0) {
            throw new RuntimeException("Số dư không đủ để xác nhận rút tiền");
        }

        user.setBalance(user.getBalance().subtract(tx.getAmount()));
        userRepository.save(user);

        tx.setStatus("SUCCESS");
        accountTransactionRepository.save(tx);

        return toResponse(tx);
    }

    // ===========================
    // GET TRANSACTION BY USER
    // ===========================
    @Override
    public List<AccountTransactionResponse> getTransactionsByUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return accountTransactionRepository.findByUser(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AccountTransactionResponse transferBetweenUsers(Long fromUserId, Long toUserId, BigDecimal amount) {
        return null;
    }

    // ===========================
    // MAPPER
    // ===========================
    private AccountTransactionResponse toResponse(AccountTransaction tx) {
        AccountTransactionResponse res = new AccountTransactionResponse();
        res.setId(tx.getTransactionId());
        res.setUserId(tx.getUser().getUserId());
        res.setAmount(tx.getAmount());
        res.setType(tx.getType());
        res.setStatus(tx.getStatus());
        res.setCreatedAt(tx.getCreatedAt());
        return res;
    }
}
