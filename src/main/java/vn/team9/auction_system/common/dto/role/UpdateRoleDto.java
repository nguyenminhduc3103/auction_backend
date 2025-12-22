package vn.team9.auction_system.common.dto.role;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateRoleDto {

    @Size(min = 1, message = "name cannot be blank")
    private String name;

    private String description;

    private Boolean isActive;

    private List<Long> permissions;
}
