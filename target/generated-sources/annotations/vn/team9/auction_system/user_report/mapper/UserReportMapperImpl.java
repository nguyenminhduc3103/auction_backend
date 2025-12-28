package vn.team9.auction_system.user_report.mapper;

import javax.annotation.processing.Generated;
import vn.team9.auction_system.common.dto.user_report.UserReportRequest;
import vn.team9.auction_system.common.dto.user_report.UserReportResponse;
import vn.team9.auction_system.user_report.model.UserReport;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-29T00:12:01+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
public class UserReportMapperImpl implements UserReportMapper {

    @Override
    public UserReport toEntity(UserReportRequest request) {
        if ( request == null ) {
            return null;
        }

        UserReport userReport = new UserReport();

        userReport.setContent( request.getContent() );
        userReport.setUserId( request.getUserId() );

        return userReport;
    }

    @Override
    public UserReportResponse toResponse(UserReport entity) {
        if ( entity == null ) {
            return null;
        }

        UserReportResponse userReportResponse = new UserReportResponse();

        userReportResponse.setContent( entity.getContent() );
        userReportResponse.setCreatedAt( entity.getCreatedAt() );
        userReportResponse.setId( entity.getId() );
        userReportResponse.setUserId( entity.getUserId() );

        return userReportResponse;
    }
}
