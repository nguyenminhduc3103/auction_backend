package vn.team9.auction_system.transaction.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.team9.auction_system.common.dto.account.AccountTransactionRequest;
import vn.team9.auction_system.common.dto.account.AccountTransactionResponse;
import vn.team9.auction_system.transaction.service.AccountTransactionServiceImpl;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("api/account")
@RequiredArgsConstructor
public class AccountTransactionController {

    private final AccountTransactionServiceImpl service;

    @PostMapping("/deposit")
    public AccountTransactionResponse deposit(@RequestBody AccountTransactionRequest request) {
        return service.deposit(request);
    }

    @PostMapping("/withdraw")
    public AccountTransactionResponse withdraw(@RequestBody AccountTransactionRequest request) {
        return service.withdraw(request);
    }

    @PostMapping("/withdraw/confirm/{id}")
    public AccountTransactionResponse confirmWithdraw(@PathVariable Long id) {
        return service.confirmWithdraw(id);
    }

    @GetMapping("/user/{userId}")
    public List<AccountTransactionResponse> getByUser(@PathVariable Long userId) {
        return service.getTransactionsByUser(userId);
    }

    @GetMapping("/user/{userId}/withdrawable")
    public BigDecimal getWithdrawable(@PathVariable Long userId) {
        return service.getWithdrawable(userId);
    }
}
