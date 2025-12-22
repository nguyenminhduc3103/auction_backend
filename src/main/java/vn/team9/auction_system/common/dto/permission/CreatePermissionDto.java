package vn.team9.auction_system.common.dto.permission;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePermissionDto {
    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "apiPath is required")
    private String apiPath;

    @NotBlank(message = "method is required")
    private String method;

    @NotBlank(message = "module is required")
    private String module;

    private String description;
}
