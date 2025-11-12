package vn.team9.auction_system.common.dto.auth;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String fullName;
    private String email;
    private String password;
    private String phone;
    private String gender;
}
