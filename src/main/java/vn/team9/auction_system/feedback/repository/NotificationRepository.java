package vn.team9.auction_system.feedback.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.team9.auction_system.feedback.model.Notification;
import vn.team9.auction_system.feedback.model.NotificationType;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserUserIdAndIsDeletedFalse(Long userId, Pageable pageable);

    Page<Notification> findByUserUserIdAndTypeAndIsDeletedFalse(
            Long userId, NotificationType type, Pageable pageable);

    Page<Notification> findByUserUserIdAndCategoryAndIsDeletedFalse(
            Long userId, String category, Pageable pageable);

    List<Notification> findByUserUserIdAndIsReadFalseAndIsDeletedFalse(Long userId);

    Long countByUserUserIdAndIsReadFalseAndIsDeletedFalse(Long userId);

    List<Notification> findByCreatedAtBeforeAndIsDeletedFalse(LocalDateTime date);

    @Query("SELECT n FROM Notification n WHERE n.user.userId = :userId AND n.isDeleted = false ORDER BY n.createdAt DESC")
    List<Notification> findLatestUnreadByUser(@Param("userId") Long userId);

    // ✅ NEW: Check if notification already exists (to prevent duplicates)
    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM Notification n " +
            "WHERE n.user.userId = :userId " +
            "AND n.category = :category " +
            "AND n.actionUrl LIKE CONCAT('%', :auctionId, '%') " +
            "AND n.isDeleted = false")
    boolean existsByUserAndCategoryAndAuction(@Param("userId") Long userId,
            @Param("category") String category,
            @Param("auctionId") String auctionId);
}
