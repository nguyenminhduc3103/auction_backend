package vn.team9.auction_system.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.team9.auction_system.auth.service.PermissionService;
import vn.team9.auction_system.common.dto.common.CreateResponse;
import vn.team9.auction_system.common.dto.pagination.PagedResponse;
import vn.team9.auction_system.common.dto.permission.CreatePermissionDto;
import vn.team9.auction_system.common.dto.permission.PermissionResponse;
import vn.team9.auction_system.common.dto.permission.UpdatePermissionDto;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @PreAuthorize("hasAuthority('POST:/api/permissions')")
    public ResponseEntity<CreateResponse> createPermission(@Valid @RequestBody CreatePermissionDto request) {
        return ResponseEntity.ok(permissionService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('GET:/api/permissions')")
    public ResponseEntity<PagedResponse<PermissionResponse>> listPermissions(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection
    ) {
        return ResponseEntity.ok(permissionService.list(current, pageSize, name, module, method, sortBy, sortDirection));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('GET:/api/permissions/{id}')")
    public ResponseEntity<PermissionResponse> getPermission(@PathVariable Long id) {
        return ResponseEntity.ok(permissionService.getOne(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('PATCH:/api/permissions/{id}')")
    public ResponseEntity<PermissionResponse> updatePermission(@PathVariable Long id,
                                                               @Valid @RequestBody UpdatePermissionDto request) {
        return ResponseEntity.ok(permissionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE:/api/permissions/{id}')")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        permissionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
