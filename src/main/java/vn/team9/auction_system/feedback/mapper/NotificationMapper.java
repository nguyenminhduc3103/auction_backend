package vn.team9.auction_system.feedback.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import vn.team9.auction_system.common.dto.notification.NotificationResponse;
import vn.team9.auction_system.feedback.model.Notification;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {
    
    // Notification entity -> NotificationResponse DTO
    @Mapping(source = "notiId", target = "notiId")
    @Mapping(source = "user.userId", target = "userId")
    NotificationResponse toResponse(Notification notification);
}
