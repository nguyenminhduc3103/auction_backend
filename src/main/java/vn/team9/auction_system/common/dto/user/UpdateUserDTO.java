package vn.team9.auction_system.common.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserDTO {
    @Size(max = 100)
    private String fullName;

    // username thường không đổi, nếu cho đổi thì validate và check uniqueness
    @Size(max = 50)
    private String username;

    @Size(max = 20)
    private String phone;

    @Size(max = 20)
    private String gender;

    // nếu admin: có thể đổi status
    private String status;
}