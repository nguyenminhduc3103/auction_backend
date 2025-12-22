package vn.team9.auction_system.common.dto.permission;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PermissionResponse {
    private Long id;
    private String name;
    private String apiPath;
    private String method;
    private String module;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
