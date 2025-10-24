package vn.team9.auction_system.feedback.mapper;

import vn.team9.auction_system.feedback.model.Feedback;
import vn.team9.auction_system.feedback.model.Notification;
import vn.team9.auction_system.feedback.model.AdminLog;
import vn.team9.auction_system.common.dto.feedback.FeedbackRequest;
import vn.team9.auction_system.common.dto.feedback.FeedbackResponse;
import vn.team9.auction_system.common.dto.notification.NotificationRequest;
import vn.team9.auction_system.common.dto.notification.NotificationResponse;
import vn.team9.auction_system.common.dto.admin.AdminLogRequest;
import vn.team9.auction_system.common.dto.admin.AdminLogResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {

    // FeedbackRequest -> Feedback entity
    Feedback toEntity(FeedbackRequest request);

    // Feedback entity -> FeedbackResponse
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "user.username", target = "username")
    FeedbackResponse toResponse(Feedback entity);

    // NotificationRequest -> Notification entity
    Notification toEntity(NotificationRequest request);

    // Notification entity -> NotificationResponse
    @Mapping(source = "user.userId", target = "userId")
    NotificationResponse toResponse(Notification entity);

    // AdminLogRequest -> AdminLog entity
    AdminLog toEntity(AdminLogRequest request);

    // AdminLog entity -> AdminLogResponse
    @Mapping(source = "admin.userId", target = "adminId")
    AdminLogResponse toResponse(AdminLog entity);
}
