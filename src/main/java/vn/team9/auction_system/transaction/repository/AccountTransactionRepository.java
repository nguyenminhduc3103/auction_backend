package vn.team9.auction_system.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.team9.auction_system.transaction.model.AccountTransaction;
import vn.team9.auction_system.user.model.User;

import java.util.List;

public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Long> {
    // Lấy tất cả transactions của 1 user với status
    List<AccountTransaction> findByUserAndStatus(User user, String status);

    List<AccountTransaction> findByUser(User user);

    // Lấy tất cả transactions của một tập user với status
    @Query("SELECT t FROM AccountTransaction t WHERE t.user IN :users AND t.status = :status")
    List<AccountTransaction> findByUsersAndStatus(@Param("users") List<User> users, @Param("status") String status);

    // Lấy transactions theo user + type + status
    List<AccountTransaction> findByUserAndTypeAndStatus(User user, String type, String status);
}
