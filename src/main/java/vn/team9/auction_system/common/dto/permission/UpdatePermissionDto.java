package vn.team9.auction_system.common.dto.permission;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePermissionDto {

    @Size(min = 1, message = "name cannot be blank")
    private String name;

    @Size(min = 1, message = "apiPath cannot be blank")
    private String apiPath;

    @Size(min = 1, message = "method cannot be blank")
    private String method;

    @Size(min = 1, message = "module cannot be blank")
    private String module;

    private String description;
}
