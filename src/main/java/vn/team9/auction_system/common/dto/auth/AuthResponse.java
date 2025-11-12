package vn.team9.auction_system.common.dto.auth;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private String username;
    private String fullName;
    private String email;
    private String gender;
    private String status;
    private String avatarUrl;
}
