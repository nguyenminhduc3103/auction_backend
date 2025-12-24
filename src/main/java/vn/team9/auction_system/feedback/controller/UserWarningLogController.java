    package vn.team9.auction_system.feedback.controller;

    import lombok.RequiredArgsConstructor;
    import org.springframework.web.bind.annotation.*;
    import vn.team9.auction_system.common.dto.admin.UserWarningLogRequest;
    import vn.team9.auction_system.common.dto.admin.UserWarningLogResponse;
    import vn.team9.auction_system.common.service.IUserWarningService;
    import vn.team9.auction_system.feedback.service.UserWarningLogImpl;

    import java.util.List;

    @RestController
    @RequestMapping("/api/warnings")
    @RequiredArgsConstructor
    public class UserWarningLogController {

        private final IUserWarningService warningService;

        @PostMapping
        public UserWarningLogResponse createWarning(@RequestBody UserWarningLogRequest request) {
            return warningService.createWarning(request);
        }
        @GetMapping
        public List<UserWarningLogResponse> getAllWarnings() {
            return warningService.getAllWarnings();
        }


        @GetMapping("/user/{userId}")
        public List<UserWarningLogResponse> getWarningsByUser(@PathVariable Long userId) {
            return warningService.getWarningsByUser(userId);
        }

        @GetMapping("/transaction/{txnId}")
        public List<UserWarningLogResponse> getWarningsByTransaction(@PathVariable Long txnId) {
            return warningService.getWarningsByTransaction(txnId);
        }

        @PostMapping("/auto-warn")
        public String autoWarnPendingTransactions() {
            if (warningService instanceof UserWarningLogImpl impl) {
                impl.warnPendingTransactionsOver36h();
                return "Auto warnings created successfully";
            }
            return "Service implementation not found";
        }
    }
