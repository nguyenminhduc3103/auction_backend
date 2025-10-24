package vn.team9.auction_system.common.service;

import vn.team9.auction_system.common.dto.notification.NotificationRequest;
import vn.team9.auction_system.common.dto.notification.NotificationResponse;
import java.util.List;

public interface INotificationService {
    NotificationResponse sendNotification(NotificationRequest request);
    List<NotificationResponse> getNotificationsByUser(Long userId);
    void markAsRead(Long notificationId);

    List<NotificationResponse> getUnreadNotificationsByUser(Long userId);
    void sendSystemNotificationToUsers(List<Long> userIds, NotificationRequest request);
}
