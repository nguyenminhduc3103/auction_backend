package vn.team9.auction_system.common.dto.feedback;

import vn.team9.auction_system.common.base.BaseResponse;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FeedbackResponse extends BaseResponse {
    private Long feedbackId;
    private Long userId;
    private String username;
    private Long productId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
}