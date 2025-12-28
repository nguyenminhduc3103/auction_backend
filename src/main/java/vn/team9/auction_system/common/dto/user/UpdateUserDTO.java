package vn.team9.auction_system.common.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserDTO {
    @Size(max = 100)
    private String fullName;

    // username usually doesn't change; if allowed, validate and check uniqueness
    @Size(max = 50)
    private String username;

    @Size(max = 20)
    private String phone;

    @Size(max = 20)
    private String gender;

    // if admin: can change status
    private String status;
}