package vn.team9.auction_system.common.dto.user;

import vn.team9.auction_system.common.base.BaseRequest;
import lombok.Data;

@Data
public class UserRequest extends BaseRequest {
    private String username;
    private String email;
    private String password;
    private String phone;
    private Long roleId;
}
