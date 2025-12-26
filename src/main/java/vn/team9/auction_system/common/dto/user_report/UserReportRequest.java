package vn.team9.auction_system.common.dto.user_report;

import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.team9.auction_system.common.base.BaseRequest;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserReportRequest extends BaseRequest {
    private Long userId;
    private String content;
}
