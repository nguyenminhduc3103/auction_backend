package vn.team9.auction_system.user_report.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import vn.team9.auction_system.user_report.model.UserReport;
import vn.team9.auction_system.common.dto.user_report.UserReportRequest;
import vn.team9.auction_system.common.dto.user_report.UserReportResponse;

@Mapper
public interface UserReportMapper {

    UserReportMapper INSTANCE = Mappers.getMapper(UserReportMapper.class);

    UserReport toEntity(UserReportRequest request);

    UserReportResponse toResponse(UserReport entity);
}
