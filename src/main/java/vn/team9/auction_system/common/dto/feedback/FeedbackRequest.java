package vn.team9.auction_system.common.dto.feedback;

import vn.team9.auction_system.common.base.BaseRequest;
import lombok.Data;

@Data
public class FeedbackRequest extends BaseRequest {
    private Long userId;
    private Long productId;
    private int rating; // 1–5
    private String comment;
}
