package vn.team9.auction_system.transaction.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('POST:/api/account/deposit')")
    public AccountTransactionResponse deposit(@RequestBody AccountTransactionRequest request) {
        return service.deposit(request);
    }

    @PostMapping("/withdraw")
    @PreAuthorize("hasAuthority('POST:/api/account/withdraw')")
    public AccountTransactionResponse withdraw(@RequestBody AccountTransactionRequest request) {
        return service.withdraw(request);
    }

    @PostMapping("/withdraw/confirm/{id}")
    @PreAuthorize("hasAuthority('POST:/api/account/withdraw/confirm/{id}')")
    public AccountTransactionResponse confirmWithdraw(@PathVariable Long id) {
        return service.confirmWithdraw(id);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('GET:/api/account/user/{userId}')")
    public List<AccountTransactionResponse> getByUser(@PathVariable Long userId) {
        return service.getTransactionsByUser(userId);
    }

    @GetMapping("/user/{userId}/withdrawable")
    @PreAuthorize("hasAuthority('GET:/api/account/user/{userId}/withdrawable')")
    public BigDecimal getWithdrawable(@PathVariable Long userId) {
        return service.getWithdrawable(userId);
    }
}
