package vn.team9.auction_system.feedback.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.team9.auction_system.feedback.model.UserWarningLog;
import vn.team9.auction_system.user.model.User;

import java.util.List;

@Repository
public interface UserWarningLogRepository extends JpaRepository<UserWarningLog, Long> {

    // Đếm số lần vi phạm loại cảnh báo cụ thể của 1 user
    int countByUserAndType(User user, String type);

    // Lấy danh sách log của user
    List<UserWarningLog> findByUser(User user);

    // Lấy danh sách log theo transaction
    List<UserWarningLog> findByTransaction_TransactionId(Long transactionId);
}
