package vn.team9.auction_system.common.service;

import vn.team9.auction_system.common.dto.user_report.UserReportRequest;
import vn.team9.auction_system.common.dto.user_report.UserReportResponse;
import java.util.List;

public interface IUserReportService {
    UserReportResponse createReport(UserReportRequest request);
    List<UserReportResponse> getReportsByUserId(Long userId);
}
