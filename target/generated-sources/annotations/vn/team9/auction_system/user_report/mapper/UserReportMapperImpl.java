package vn.team9.auction_system.user_report.mapper;

import javax.annotation.processing.Generated;
import vn.team9.auction_system.common.dto.user_report.UserReportRequest;
import vn.team9.auction_system.common.dto.user_report.UserReportResponse;
import vn.team9.auction_system.user_report.model.UserReport;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-29T20:07:29+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 22.0.2 (Oracle Corporation)"
)
public class UserReportMapperImpl implements UserReportMapper {

    @Override
    public UserReport toEntity(UserReportRequest request) {
        if ( request == null ) {
            return null;
        }

        UserReport userReport = new UserReport();

        userReport.setUserId( request.getUserId() );
        userReport.setContent( request.getContent() );
        userReport.setAuctionId( request.getAuctionId() );
        userReport.setSellerId( request.getSellerId() );

        return userReport;
    }

    @Override
    public UserReportResponse toResponse(UserReport entity) {
        if ( entity == null ) {
            return null;
        }

        UserReportResponse userReportResponse = new UserReportResponse();

        userReportResponse.setId( entity.getId() );
        userReportResponse.setUserId( entity.getUserId() );
        userReportResponse.setContent( entity.getContent() );
        userReportResponse.setAuctionId( entity.getAuctionId() );
        userReportResponse.setSellerId( entity.getSellerId() );
        userReportResponse.setCreatedAt( entity.getCreatedAt() );

        return userReportResponse;
    }
}
