package vn.team9.auction_system.user_report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.team9.auction_system.user_report.model.UserReport;
import java.util.List;

public interface UserReportRepository extends JpaRepository<UserReport, Long> {
    List<UserReport> findByUserId(Long userId);
}
