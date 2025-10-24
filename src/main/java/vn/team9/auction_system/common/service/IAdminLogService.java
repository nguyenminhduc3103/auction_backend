package vn.team9.auction_system.common.service;

import vn.team9.auction_system.common.dto.admin.AdminLogRequest;
import vn.team9.auction_system.common.dto.admin.AdminLogResponse;
import java.util.List;

public interface IAdminLogService {
    AdminLogResponse performAction(AdminLogRequest request);
    List<AdminLogResponse> getAllActions();
    AdminLogResponse getActionById(Long id);

    List<AdminLogResponse> getActionsByAdmin(Long adminId);
    List<AdminLogResponse> queryLogs(String keyword);   // tìm log theo nội dung/hành động
}
