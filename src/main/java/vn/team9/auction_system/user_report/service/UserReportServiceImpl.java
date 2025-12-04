package vn.team9.auction_system.user_report.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import vn.team9.auction_system.user_report.repository.UserReportRepository;
import vn.team9.auction_system.user_report.model.UserReport;
import vn.team9.auction_system.user_report.mapper.UserReportMapper;
import vn.team9.auction_system.common.service.IUserReportService;
import vn.team9.auction_system.common.dto.user_report.UserReportRequest;
import vn.team9.auction_system.common.dto.user_report.UserReportResponse;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserReportServiceImpl implements IUserReportService {

    private final UserReportRepository userReportRepository;

    @Override
    public UserReportResponse createReport(UserReportRequest request) {
        UserReport report = UserReportMapper.INSTANCE.toEntity(request);
        UserReport saved = userReportRepository.save(report);
        return UserReportMapper.INSTANCE.toResponse(saved);
    }

    @Override
    public List<UserReportResponse> getReportsByUserId(Long userId) {
        return userReportRepository.findByUserId(userId)
                .stream()
                .map(UserReportMapper.INSTANCE::toResponse)
                .collect(Collectors.toList());
    }
}