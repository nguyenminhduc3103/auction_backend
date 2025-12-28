package vn.team9.auction_system.transaction.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.team9.auction_system.auction.model.Bid;
import vn.team9.auction_system.auction.repository.BidRepository;
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
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountTransactionServiceImpl implements IAccountTransactionService {

    private final AccountTransactionRepository accountTransactionRepository;
    private final UserRepository userRepository;
    private final BidRepository bidRepository;

    // -------------------------------------------------------
    // FUNCTION 1: Calculate locked amount due to holding highest bids
    // -------------------------------------------------------
    public BigDecimal getLockedByBids(Long userId) {
        List<Bid> highestBids = bidRepository.findByBidder_UserId(userId)
                .stream()
                .filter(b -> Boolean.TRUE.equals(b.getIsHighest()))
                .filter(b -> b.getAuction().getStatus().equals("OPEN")
                        || b.getAuction().getStatus().equals("PENDING"))
                .toList();

        return highestBids.stream()
                .map(Bid::getBidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // -------------------------------------------------------
    // FUNCTION 2: Calculate locked amount due to SHIPPED transactions (not paid)
    // -------------------------------------------------------
    public BigDecimal getLockedByAuctionTransactions(Long userId) {
        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1) Escrow amount: buyer has PAY (balance deducted) -> AccountTransaction type = "TRANSFER" status = "PENDING"
        List<AccountTransaction> escrows = accountTransactionRepository
                .findByUserAndTypeAndStatus(user, "TRANSFER", "PENDING");

        BigDecimal escrowLocked = escrows.stream()
                // buyer TRANSFER has negative amount (e.g. -50.00). Take absolute value.
                .map(t -> t.getAmount().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2) Withdraw pending amount (user requested withdrawal)
        List<AccountTransaction> withdrawPendings = accountTransactionRepository
                .findByUserAndTypeAndStatus(user, "WITHDRAW", "PENDING");

        BigDecimal withdrawPendingLocked = withdrawPendings.stream()
                .map(AccountTransaction::getAmount) // withdraw amount is positive (requested)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total locked amount from account transactions
        return escrowLocked.add(withdrawPendingLocked);
    }

    // -------------------------------------------------------
    // FUNCTION 3: Calculate withdrawable balance
    // -------------------------------------------------------
    public BigDecimal getWithdrawable(Long userId) {
        log.error("=== DEBUG getWithdrawable (NEW LOGIC) ===");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.error("Total balance: {}", user.getBalance());

        // 1) Locked by highest bids (only count is_highest = 1)
        BigDecimal lockedBids = getLockedByBids(userId);
        log.error("Locked by bids (is_highest=1 only): {}", lockedBids);

        // 2) Locked by account transactions
        BigDecimal lockedByAccountTx = getLockedByAuctionTransactions(userId);
        log.error("Locked by account tx: {}", lockedByAccountTx);

        // withdrawable = balance - lockedBids - lockedByAccountTx
        BigDecimal withdrawable = user.getBalance()
                .subtract(lockedBids)
                .subtract(lockedByAccountTx);

        log.error("Final withdrawable: {}", withdrawable);

        return withdrawable;
    }

    // -------------------------------------------------------
    // DEPOSIT
    // -------------------------------------------------------
    @Transactional
    @Override
    public AccountTransactionResponse deposit(AccountTransactionRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setBalance(user.getBalance().add(request.getAmount()));
        userRepository.save(user);

        AccountTransaction tx = new AccountTransaction();
        tx.setUser(user);
        tx.setAmount(request.getAmount());
        tx.setType("DEPOSIT");
        tx.setStatus("SUCCESS");
        tx.setCreatedAt(LocalDateTime.now());

        accountTransactionRepository.save(tx);

        return toResponse(tx);
    }

    // -------------------------------------------------------
    // WITHDRAW (PENDING)
    // -------------------------------------------------------
    @Transactional
    @Override
    public AccountTransactionResponse withdraw(AccountTransactionRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal withdrawable = getWithdrawable(user.getUserId());

        if (withdrawable.compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient available balance to withdraw");
        }

        AccountTransaction tx = new AccountTransaction();
        tx.setUser(user);
        tx.setAmount(request.getAmount());
        tx.setType("WITHDRAW");
        tx.setStatus("PENDING");
        tx.setCreatedAt(LocalDateTime.now());

        accountTransactionRepository.save(tx);

        return toResponse(tx);
    }

    // -------------------------------------------------------
    // CONFIRM WITHDRAW
    // -------------------------------------------------------
    @Transactional
    public AccountTransactionResponse confirmWithdraw(Long transactionId) {

        AccountTransaction tx = accountTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!"PENDING".equals(tx.getStatus())) {
            throw new RuntimeException("Withdraw must be in PENDING status");
        }

        User user = tx.getUser();

        BigDecimal lockedAuction = getLockedByAuctionTransactions(user.getUserId());

        BigDecimal withdrawable = user.getBalance()
                .subtract(getLockedByBids(user.getUserId()))
                .subtract(lockedAuction);

        if (withdrawable.compareTo(tx.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance after calculating locked amount");
        }
        // Deduct money
        user.setBalance(user.getBalance().subtract(tx.getAmount()));
        userRepository.save(user);

        tx.setStatus("SUCCESS");
        accountTransactionRepository.save(tx);

        return toResponse(tx);
    }

    // -------------------------------------------------------
    // Get transaction list
    // -------------------------------------------------------
    @Override
    public Page<AccountTransactionResponse> getTransactionsByUser(
            Long userId,
            String status,
            String type,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<AccountTransaction> pageResult =
                accountTransactionRepository.search(
                        user,
                        status,
                        type,
                        from,
                        to,
                        pageable
                );

        return pageResult.map(this::toResponse);
    }

    @Override
    public AccountTransactionResponse transferBetweenUsers(Long fromUserId, Long toUserId, BigDecimal amount) {
        return null;
    }

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