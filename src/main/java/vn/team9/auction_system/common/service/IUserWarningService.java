package vn.team9.auction_system.common.service;

import vn.team9.auction_system.common.dto.admin.UserWarningLogRequest;
import vn.team9.auction_system.common.dto.admin.UserWarningLogResponse;

import java.util.List;

public interface IUserWarningService {

    // Create warning log
    UserWarningLogResponse createWarning(UserWarningLogRequest request);

    // Get logs by user
    List<UserWarningLogResponse> getWarningsByUser(Long userId);

    // Get logs by transaction
    List<UserWarningLogResponse> getWarningsByTransaction(Long transactionId);

    // Get all warning logs
    List<UserWarningLogResponse> getAllWarnings();

    void processOverdueTransactions();
}