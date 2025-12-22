package vn.team9.auction_system.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.team9.auction_system.auth.service.RoleService;
import vn.team9.auction_system.common.dto.common.CreateResponse;
import vn.team9.auction_system.common.dto.pagination.PagedResponse;
import vn.team9.auction_system.common.dto.role.CreateRoleDto;
import vn.team9.auction_system.common.dto.role.RoleResponse;
import vn.team9.auction_system.common.dto.role.UpdateRoleDto;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @PreAuthorize("hasAuthority('POST:/api/roles')")
    public ResponseEntity<CreateResponse> createRole(@Valid @RequestBody CreateRoleDto request) {
        return ResponseEntity.ok(roleService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('GET:/api/roles')")
    public ResponseEntity<PagedResponse<RoleResponse>> listRoles(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection
    ) {
        return ResponseEntity.ok(roleService.list(current, pageSize, name, isActive, sortBy, sortDirection));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('GET:/api/roles/{id}')")
    public ResponseEntity<RoleResponse> getRole(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getOne(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('PATCH:/api/roles/{id}')")
    public ResponseEntity<RoleResponse> updateRole(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateRoleDto request) {
        return ResponseEntity.ok(roleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE:/api/roles/{id}')")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
