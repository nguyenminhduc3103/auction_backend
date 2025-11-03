package vn.team9.auction_system.auth.dto;

import lombok.*;

import java.time.LocalDateTime;

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
}
