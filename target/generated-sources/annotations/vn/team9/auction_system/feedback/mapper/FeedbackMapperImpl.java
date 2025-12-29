package vn.team9.auction_system.feedback.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import vn.team9.auction_system.common.dto.admin.UserWarningLogRequest;
import vn.team9.auction_system.common.dto.admin.UserWarningLogResponse;
import vn.team9.auction_system.common.dto.feedback.FeedbackRequest;
import vn.team9.auction_system.common.dto.feedback.FeedbackResponse;
import vn.team9.auction_system.feedback.model.Feedback;
import vn.team9.auction_system.feedback.model.UserWarningLog;
import vn.team9.auction_system.transaction.model.TransactionAfterAuction;
import vn.team9.auction_system.user.model.User;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-29T13:19:25+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class FeedbackMapperImpl implements FeedbackMapper {

    @Override
    public Feedback toEntity(FeedbackRequest request) {
        if ( request == null ) {
            return null;
        }

        Feedback feedback = new Feedback();

        feedback.setComment( request.getComment() );
        feedback.setRating( request.getRating() );

        return feedback;
    }

    @Override
    public FeedbackResponse toResponse(Feedback entity) {
        if ( entity == null ) {
            return null;
        }

        FeedbackResponse feedbackResponse = new FeedbackResponse();

        feedbackResponse.setUserId( entityUserUserId( entity ) );
        feedbackResponse.setUsername( entityUserUsername( entity ) );
        feedbackResponse.setComment( entity.getComment() );
        feedbackResponse.setCreatedAt( entity.getCreatedAt() );
        feedbackResponse.setFeedbackId( entity.getFeedbackId() );
        if ( entity.getRating() != null ) {
            feedbackResponse.setRating( entity.getRating() );
        }

        return feedbackResponse;
    }

    @Override
    public UserWarningLog toEntity(UserWarningLogRequest request) {
        if ( request == null ) {
            return null;
        }

        UserWarningLog userWarningLog = new UserWarningLog();

        userWarningLog.setDescription( request.getDescription() );
        userWarningLog.setStatus( request.getStatus() );
        userWarningLog.setType( request.getType() );
        userWarningLog.setViolationCount( request.getViolationCount() );

        return userWarningLog;
    }

    @Override
    public UserWarningLogResponse toResponse(UserWarningLog entity) {
        if ( entity == null ) {
            return null;
        }

        UserWarningLogResponse userWarningLogResponse = new UserWarningLogResponse();

        userWarningLogResponse.setUserId( entityUserUserId1( entity ) );
        userWarningLogResponse.setTransactionId( entityTransactionTransactionId( entity ) );
        userWarningLogResponse.setCreatedAt( entity.getCreatedAt() );
        userWarningLogResponse.setDescription( entity.getDescription() );
        userWarningLogResponse.setLogId( entity.getLogId() );
        userWarningLogResponse.setStatus( entity.getStatus() );
        userWarningLogResponse.setType( entity.getType() );
        userWarningLogResponse.setViolationCount( entity.getViolationCount() );

        return userWarningLogResponse;
    }

    private Long entityUserUserId(Feedback feedback) {
        if ( feedback == null ) {
            return null;
        }
        User user = feedback.getUser();
        if ( user == null ) {
            return null;
        }
        Long userId = user.getUserId();
        if ( userId == null ) {
            return null;
        }
        return userId;
    }

    private String entityUserUsername(Feedback feedback) {
        if ( feedback == null ) {
            return null;
        }
        User user = feedback.getUser();
        if ( user == null ) {
            return null;
        }
        String username = user.getUsername();
        if ( username == null ) {
            return null;
        }
        return username;
    }

    private Long entityUserUserId1(UserWarningLog userWarningLog) {
        if ( userWarningLog == null ) {
            return null;
        }
        User user = userWarningLog.getUser();
        if ( user == null ) {
            return null;
        }
        Long userId = user.getUserId();
        if ( userId == null ) {
            return null;
        }
        return userId;
    }

    private Long entityTransactionTransactionId(UserWarningLog userWarningLog) {
        if ( userWarningLog == null ) {
            return null;
        }
        TransactionAfterAuction transaction = userWarningLog.getTransaction();
        if ( transaction == null ) {
            return null;
        }
        Long transactionId = transaction.getTransactionId();
        if ( transactionId == null ) {
            return null;
        }
        return transactionId;
    }
}
