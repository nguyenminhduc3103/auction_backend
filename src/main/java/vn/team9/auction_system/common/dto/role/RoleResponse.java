package vn.team9.auction_system.common.dto.role;

import lombok.Data;
import vn.team9.auction_system.common.dto.permission.PermissionResponse;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RoleResponse {
    private Long id;
    private String name;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PermissionResponse> permissions;
}
