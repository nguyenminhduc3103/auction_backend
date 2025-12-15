package vn.team9.auction_system.common.dto.user_report;

import lombok.Data;
import vn.team9.auction_system.common.base.BaseResponse;
import java.time.LocalDateTime;

@Data
public class UserReportResponse extends BaseResponse {
    private Long id;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
}
