package vn.team9.auction_system.feedback.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import vn.team9.auction_system.common.dto.notification.NotificationResponse;
import vn.team9.auction_system.feedback.service.NotificationService;
import vn.team9.auction_system.user.model.User;
import vn.team9.auction_system.user.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    /**
     * Get all notifications for the authenticated user with pagination
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Long userId = extractUserIdFromUserDetails(userDetails);

            // Parse sort parameter
            String[] sortParts = sort.split(",");
            Sort.Direction direction = "asc".equalsIgnoreCase(sortParts.length > 1 ? sortParts[1] : "asc")
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
            String sortField = sortParts[0];

            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

            Page<NotificationResponse> notifications;
            if (type != null && !type.isEmpty()) {
                notifications = notificationService.getNotificationsByUserAndType(userId, type, pageable);
            } else if (category != null && !category.isEmpty()) {
                notifications = notificationService.getNotificationsByUserAndCategory(userId, category, pageable);
            } else {
                notifications = notificationService.getNotificationsByUser(userId, pageable);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("content", notifications.getContent());
            response.put("totalElements", notifications.getTotalElements());
            response.put("totalPages", notifications.getTotalPages());
            response.put("currentPage", notifications.getNumber());
            response.put("pageSize", notifications.getSize());
            response.put("hasNext", notifications.hasNext());
            response.put("hasPrevious", notifications.hasPrevious());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching notifications", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Legacy endpoint - Get notifications by userId
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getNotificationsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category) {

        try {
            // Parse sort parameter
            String sortField = "createdAt";
            Sort.Direction direction = Sort.Direction.DESC;

            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

            Page<NotificationResponse> notifications;
            if (type != null && !type.isEmpty()) {
                notifications = notificationService.getNotificationsByUserAndType(userId, type, pageable);
            } else if (category != null && !category.isEmpty()) {
                notifications = notificationService.getNotificationsByUserAndCategory(userId, category, pageable);
            } else {
                notifications = notificationService.getNotificationsByUser(userId, pageable);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("content", notifications.getContent());
            response.put("totalElements", notifications.getTotalElements());
            response.put("totalPages", notifications.getTotalPages());
            response.put("currentPage", notifications.getNumber());
            response.put("pageSize", notifications.getSize());
            response.put("hasNext", notifications.hasNext());
            response.put("hasPrevious", notifications.hasPrevious());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching notifications for user: {}", userId, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Mark a single notification as read
     */
    @PutMapping("/{id}/read")
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {

        try {
            notificationService.markAsRead(id);
            log.info("Notification {} marked as read", id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error marking notification as read: {}", id, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Mark all notifications as read for user
     */
    @PutMapping("/user/{userId}/read-all")
    @PatchMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {

        try {
            notificationService.markAllAsReadByUser(userId);
            log.info("All notifications marked as read for user: {}", userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error marking all notifications as read", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Mark notifications by type as read
     */
    @PatchMapping("/read-by-type")
    public ResponseEntity<Void> markAsReadByType(
            @RequestParam String type,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Long userId = extractUserIdFromUserDetails(userDetails);
            notificationService.markAsReadByType(userId, type);
            log.info("Notifications of type {} marked as read for user: {}", type, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error marking notifications as read by type", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Get unread notification count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Long userId = extractUserIdFromUserDetails(userDetails);
            Long unreadCount = notificationService.getUnreadCount(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("unreadCount", unreadCount);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching unread count", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Delete a notification
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            notificationService.deleteNotification(id);
            log.info("Notification {} deleted", id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting notification: {}", id, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Extract user ID from UserDetails
     */
    private Long extractUserIdFromUserDetails(UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new RuntimeException("User not authenticated");
        }

        // Try to get user by username
        String username = userDetails.getUsername();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return user.getUserId();
    }
}
