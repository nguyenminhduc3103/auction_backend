package vn.team9.auction_system.common.service;

import vn.team9.auction_system.common.dto.feedback.FeedbackRequest;
import vn.team9.auction_system.common.dto.feedback.FeedbackResponse;
import java.util.List;

public interface IFeedbackService {
    FeedbackResponse createFeedback(FeedbackRequest request);
    List<FeedbackResponse> getFeedbacksByProduct(Long productId);

    List<FeedbackResponse> getFeedbacksByUser(Long userId);
}
