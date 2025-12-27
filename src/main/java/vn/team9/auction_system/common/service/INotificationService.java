package vn.team9.auction_system.common.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.team9.auction_system.common.dto.notification.NotificationRequest;
import vn.team9.auction_system.common.dto.notification.NotificationResponse;
import java.util.List;

public interface INotificationService {
    NotificationResponse sendNotification(NotificationRequest request);
    
    Page<NotificationResponse> getNotificationsByUser(Long userId, Pageable pageable);
    
    Page<NotificationResponse> getNotificationsByUserAndType(Long userId, String type, Pageable pageable);
    
    Page<NotificationResponse> getNotificationsByUserAndCategory(Long userId, String category, Pageable pageable);
    
    void markAsRead(Long notificationId);
    
    void markAllAsReadByUser(Long userId);

    List<NotificationResponse> getUnreadNotificationsByUser(Long userId);
    
    void sendSystemNotificationToUsers(List<Long> userIds, NotificationRequest request);
    
    Long getUnreadCount(Long userId);
    
    void deleteNotification(Long notificationId);
    
    void deleteOldNotifications(int daysOld);
}
