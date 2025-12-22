package vn.team9.auction_system.common.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateRoleDto {
    @NotBlank(message = "name is required")
    private String name;

    private String description;

    @NotNull(message = "isActive is required")
    private Boolean isActive;

    @NotEmpty(message = "permissions cannot be empty")
    private List<Long> permissions;
}
