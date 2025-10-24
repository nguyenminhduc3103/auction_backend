package vn.team9.auction_system.common.dto.admin;

import lombok.Data;

@Data
public class AdminLogRequest {
    private Long adminId;
    private String action; // e.g. APPROVE_PRODUCT, BAN_USER, REFUND
    private String target; // target entity (product, user, feedback...)
    private String description;
}
