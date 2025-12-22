package vn.team9.auction_system.user_report.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.team9.auction_system.common.dto.user_report.UserReportRequest;
import vn.team9.auction_system.common.dto.user_report.UserReportResponse;
import vn.team9.auction_system.user_report.service.UserReportServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/api/user-reports")
@RequiredArgsConstructor
public class UserReportController {

    private final UserReportServiceImpl userReportService;

    @PostMapping
    public UserReportResponse createReport(@RequestBody UserReportRequest request) {
        return userReportService.createReport(request);
    }

    @GetMapping("/user/{userId}")
    public List<UserReportResponse> getReportsByUser(@PathVariable Long userId) {
        return userReportService.getReportsByUserId(userId);
    }
    @GetMapping
    public List<UserReportResponse> getAllReports() {
    return userReportService.getAllReports();
}
}
