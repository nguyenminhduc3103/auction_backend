package vn.team9.auction_system.common.service;

import vn.team9.auction_system.common.dto.admin.UserWarningLogRequest;
import vn.team9.auction_system.common.dto.admin.UserWarningLogResponse;

import java.util.List;

public interface IUserWarningService {

    // Tạo log cảnh báo
    UserWarningLogResponse createWarning(UserWarningLogRequest request);

    // Lấy log theo user
    List<UserWarningLogResponse> getWarningsByUser(Long userId);

    // Lấy log theo transaction
    List<UserWarningLogResponse> getWarningsByTransaction(Long transactionId);
}
