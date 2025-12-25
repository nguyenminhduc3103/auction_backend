package vn.team9.auction_system.feedback.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.team9.auction_system.feedback.model.UserWarningLog;
import vn.team9.auction_system.user.model.User;

import java.util.List;

@Repository
public interface UserWarningLogRepository extends JpaRepository<UserWarningLog, Long> {

    // Count number of specific warning type violations for a user
    int countByUserAndType(User user, String type);

    // Get log list of a user
    List<UserWarningLog> findByUser(User user);

    // Get log list by transaction
    List<UserWarningLog> findByTransaction_TransactionId(Long transactionId);
}