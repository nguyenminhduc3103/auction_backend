package vn.team9.auction_system.common.dto.feedback;

import lombok.EqualsAndHashCode;
import vn.team9.auction_system.common.base.BaseRequest;
import lombok.Data;

@EqualsAndHashCode(callSuper = true)
@Data
public class FeedbackRequest extends BaseRequest {
    private Long userId;
    private Long productId;
    private int rating;
    private String comment;
}
