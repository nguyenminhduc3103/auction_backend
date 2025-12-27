package vn.team9.auction_system.feedback.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.team9.auction_system.common.dto.notification.NotificationRequest;
import vn.team9.auction_system.common.dto.notification.NotificationResponse;
import vn.team9.auction_system.common.service.INotificationService;
import vn.team9.auction_system.feedback.mapper.NotificationMapper;
import vn.team9.auction_system.feedback.model.Notification;
import vn.team9.auction_system.feedback.model.NotificationPriority;
import vn.team9.auction_system.feedback.model.NotificationType;
import vn.team9.auction_system.feedback.repository.NotificationRepository;
import vn.team9.auction_system.user.model.User;
import vn.team9.auction_system.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper mapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public NotificationResponse sendNotification(NotificationRequest request) {
        // Validate user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("📌 sendNotification called:");
        log.info("   userId: {}", request.getUserId());
        log.info("   userRole: {}", user.getRole() != null ? user.getRole().getRoleName() : "NULL");
        log.info("   category: {}", request.getCategory());
        log.info("   type: {}", request.getType());

        // Check if notification is allowed for user's role
        if (!isNotificationAllowedForRole(user, request.getCategory())) {
            log.warn("❌ Notification BLOCKED for user: {}, role: {}, category: {}",
                    request.getUserId(),
                    user.getRole() != null ? user.getRole().getRoleName() : "NULL",
                    request.getCategory());
            return null;
        }

        log.info("✅ Notification ALLOWED for user: {}", request.getUserId());

        // Build notification entity
        Notification notification = Notification.builder()
                .user(user)
                .title(request.getTitle())
                .message(request.getMessage())
                .type(NotificationType.valueOf(request.getType().toUpperCase()))
                .category(request.getCategory())
                .priority(request.getPriority() != null
                        ? NotificationPriority.valueOf(request.getPriority().toUpperCase())
                        : NotificationPriority.MEDIUM)
                .isRead(false)
                .actionUrl(request.getActionUrl())
                .actionLabel(request.getActionLabel())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build();

        // Save to database
        Notification saved = notificationRepository.save(notification);
        log.info("Notification created: {} for user: {}", saved.getNotiId(), user.getUserId());

        // Push notification to WebSocket client in real-time
        try {
            NotificationResponse response = mapper.toResponse(saved);
            Map<String, Object> wsMessage = new HashMap<>();
            wsMessage.put("status", "notification");
            wsMessage.put("message", "New notification received");
            wsMessage.put("notification", response);
            wsMessage.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSendToUser(
                    String.valueOf(user.getUserId()),
                    "/queue/notifications",
                    wsMessage);
            log.info("Notification pushed to WebSocket for user: {}", user.getUserId());
        } catch (Exception e) {
            log.warn("Failed to push notification to WebSocket (user may be offline): {}", e.getMessage());
        }

        return mapper.toResponse(saved);
    }

    @Override
    public Page<NotificationResponse> getNotificationsByUser(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository
                .findByUserUserIdAndIsDeletedFalse(userId, pageable);
        return notifications.map(mapper::toResponse);
    }

    @Override
    public Page<NotificationResponse> getNotificationsByUserAndType(
            Long userId, String type, Pageable pageable) {
        NotificationType notificationType = NotificationType.valueOf(type.toUpperCase());
        Page<Notification> notifications = notificationRepository
                .findByUserUserIdAndTypeAndIsDeletedFalse(userId, notificationType, pageable);
        return notifications.map(mapper::toResponse);
    }

    public Page<NotificationResponse> getNotificationsByUserAndCategory(
            Long userId, String category, Pageable pageable) {
        Page<Notification> notifications = notificationRepository
                .findByUserUserIdAndCategoryAndIsDeletedFalse(userId, category, pageable);
        return notifications.map(mapper::toResponse);
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
        log.info("Notification marked as read: {}", notificationId);
    }

    @Override
    public void markAllAsReadByUser(Long userId) {
        List<Notification> unreadNotifications = notificationRepository
                .findByUserUserIdAndIsReadFalseAndIsDeletedFalse(userId);

        LocalDateTime now = LocalDateTime.now();
        unreadNotifications.forEach(n -> {
            n.setIsRead(true);
            n.setReadAt(now);
        });
        notificationRepository.saveAll(unreadNotifications);
        log.info("All notifications marked as read for user: {}", userId);
    }

    public void markAsReadByType(Long userId, String type) {
        NotificationType notificationType = NotificationType.valueOf(type.toUpperCase());
        List<Notification> notifications = notificationRepository
                .findByUserUserIdAndTypeAndIsDeletedFalse(userId, notificationType,
                        org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE))
                .getContent();

        LocalDateTime now = LocalDateTime.now();
        notifications.forEach(n -> {
            if (!n.getIsRead()) {
                n.setIsRead(true);
                n.setReadAt(now);
            }
        });
        notificationRepository.saveAll(notifications);
        log.info("Marked {} notifications as read for user {} with type {}",
                notifications.size(), userId, type);
    }

    @Override
    public List<NotificationResponse> getUnreadNotificationsByUser(Long userId) {
        List<Notification> unread = notificationRepository
                .findByUserUserIdAndIsReadFalseAndIsDeletedFalse(userId);
        return unread.stream().map(mapper::toResponse).toList();
    }

    @Override
    public void sendSystemNotificationToUsers(List<Long> userIds, NotificationRequest request) {
        for (Long userId : userIds) {
            request.setUserId(userId);
            sendNotification(request);
        }
        log.info("System notification sent to {} users", userIds.size());
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return notificationRepository
                .countByUserUserIdAndIsReadFalseAndIsDeletedFalse(userId);
    }

    public void deleteNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsDeleted(true);
        notificationRepository.save(notification);
        log.info("Notification deleted (soft delete): {}", notificationId);
    }

    public void deleteOldNotifications(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        List<Notification> oldNotifications = notificationRepository
                .findByCreatedAtBeforeAndIsDeletedFalse(cutoffDate);
        oldNotifications.forEach(n -> n.setIsDeleted(true));
        notificationRepository.saveAll(oldNotifications);
        log.info("Deleted {} old notifications (> {} days)", oldNotifications.size(), daysOld);
    }

    /**
     * Check if notification is allowed for user's role
     * 
     * BIDDER can receive: BID_PLACED, OUTBID, AUCTION_ENDING, AUCTION_WON,
     * AUCTION_LOST,
     * PAYMENT_DUE, PAYMENT_SUCCESS, PAYMENT_FAILED, SHIPMENT_CONFIRMED
     * SELLER can receive: PRODUCT_APPROVED, PRODUCT_REJECTED
     * ADMIN can receive: All notifications (ANNOUNCEMENT, etc.)
     */
    private boolean isNotificationAllowedForRole(User user, String notificationCategory) {
        if (user.getRole() == null) {
            log.warn("⚠️ User role is NULL for user: {}", user.getUserId());
            return false;
        }

        String roleName = user.getRole().getRoleName().toUpperCase();
        log.info("   Checking role: '{}' for category: '{}'", roleName, notificationCategory);

        boolean allowed = false;
        switch (roleName) {
            case "BIDDER":
                allowed = isBidderNotification(notificationCategory);
                break;
            case "SELLER":
                allowed = isSellerNotification(notificationCategory);
                break;
            case "ADMIN":
                allowed = true; // Admin receives all notifications
                break;
            default:
                log.warn("⚠️ Unknown role: {}", roleName);
                allowed = false;
        }

        log.info("   Role check result: {} (allowed={})", allowed ? "✅ ALLOWED" : "❌ NOT ALLOWED", allowed);
        return allowed;
    }

    private boolean isBidderNotification(String category) {
        if (category == null)
            return false;

        boolean isAllowed = category.equals("BID_PLACED") ||
                category.equals("OUTBID") ||
                category.equals("AUCTION_ENDING_SOON") ||
                category.equals("AUCTION_WON") ||
                category.equals("AUCTION_LOST") ||
                category.equals("LEADING_BID") ||
                category.equals("PAYMENT_DUE") ||
                category.equals("PAYMENT_SUCCESS") ||
                category.equals("PAYMENT_FAILED") ||
                category.equals("SHIPMENT_CONFIRMED") ||
                category.equals("TRANSACTION_COMPLETED") ||
                category.equals("TRANSACTION_CANCELLED");

        if (!isAllowed) {
            log.debug("     Category '{}' NOT in BIDDER allowed list", category);
        }
        return isAllowed;
    }

    private boolean isSellerNotification(String category) {
        if (category == null)
            return false;

        boolean isAllowed = category.equals("PRODUCT_APPROVED") ||
                category.equals("PRODUCT_REJECTED") ||
                category.equals("AUCTION_APPROVED") ||
                category.equals("AUCTION_REJECTED") ||
                category.equals("AUCTION_STARTED") ||
                category.equals("AUCTION_ENDED") ||
                category.equals("HIGHEST_BID_CHANGED") ||
                category.equals("PAYMENT_RECEIVED") ||
                category.equals("TRANSACTION_COMPLETED") ||
                category.equals("TRANSACTION_CANCELLED");

        if (!isAllowed) {
            log.debug("     Category '{}' NOT in SELLER allowed list", category);
        }
        return isAllowed;
    }

}
