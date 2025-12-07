package vn.team9.auction_system.common.dto.user_report;

import lombok.Data;
import vn.team9.auction_system.common.base.BaseRequest;

@Data
public class UserReportRequest extends BaseRequest {
    private Long userId;
    private String content;
}
