package vn.team9.auction_system.feedback.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.team9.auction_system.common.dto.admin.UserWarningLogRequest;
import vn.team9.auction_system.common.dto.admin.UserWarningLogResponse;
import vn.team9.auction_system.common.service.IUserWarningService;

import java.util.List;

@RestController
@RequestMapping("/api/warnings")
@RequiredArgsConstructor
public class UserWarningLogController {

    private final IUserWarningService warningService;

    @PostMapping
    @PreAuthorize("hasAuthority('POST:/api/warnings')")
    public UserWarningLogResponse createWarning(@RequestBody UserWarningLogRequest request) {
        return warningService.createWarning(request);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('GET:/api/warnings')")
    public List<UserWarningLogResponse> getAllWarnings() {
        return warningService.getAllWarnings();
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('GET:/api/warnings/user/{userId}')")
    public List<UserWarningLogResponse> getWarningsByUser(@PathVariable Long userId) {
        return warningService.getWarningsByUser(userId);
    }

    @GetMapping("/transaction/{txnId}")
    @PreAuthorize("hasAuthority('GET:/api/warnings/transaction/{txnId}')")
    public List<UserWarningLogResponse> getWarningsByTransaction(@PathVariable Long txnId) {
        return warningService.getWarningsByTransaction(txnId);
    }

    @PostMapping("/auto-warn")
    @PreAuthorize("hasAuthority('POST:/api/warnings/auto-warn')")
    public String autoWarnPendingTransactions() {
        warningService.processOverdueTransactions();
        return "Auto warnings created successfully";
    }
}
