package vn.team9.auction_system.auth.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import vn.team9.auction_system.auth.model.Permission;
import vn.team9.auction_system.auth.repository.PermissionRepository;
import vn.team9.auction_system.auth.repository.RoleRepository;
import vn.team9.auction_system.common.dto.pagination.PagedResponse;
import vn.team9.auction_system.common.dto.role.CreateRoleDto;
import vn.team9.auction_system.common.dto.role.RoleResponse;
import vn.team9.auction_system.common.exception.BadRequestException;
import vn.team9.auction_system.user.model.User;
import vn.team9.auction_system.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RoleServiceTest {

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    private Long currentUserId;
    private Long permissionAId;
    private Long permissionBId;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("admin@test.com");
        user.setUsername("admin");
        user.setPasswordHash("password");
        user.setCreatedAt(LocalDateTime.now());
        currentUserId = userRepository.save(user).getUserId();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), null, List.of())
        );

        permissionAId = createPermission("View users", "/api/users", "GET");
        permissionBId = createPermission("Manage products", "/api/products", "POST");
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createRole_success() {
        CreateRoleDto dto = new CreateRoleDto();
        dto.setName("MANAGER");
        dto.setDescription("Manager role");
        dto.setIsActive(true);
        dto.setPermissions(List.of(permissionAId, permissionBId));

        var created = roleService.create(dto);

        assertNotNull(created.getId());
        assertNotNull(created.getCreatedAt());
    }

    @Test
    void createRole_duplicateName_throwsBadRequest() {
        CreateRoleDto dto = new CreateRoleDto();
        dto.setName("EDITOR");
        dto.setDescription("Editor role");
        dto.setIsActive(true);
        dto.setPermissions(List.of(permissionAId));
        roleService.create(dto);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> roleService.create(dto));
        assertEquals("Role already exists", ex.getMessage());
    }

    @Test
    void listRoles_returnsPaginationMeta() {
        for (int i = 0; i < 3; i++) {
            CreateRoleDto dto = new CreateRoleDto();
            dto.setName("ROLE_" + i);
            dto.setDescription("Role number " + i);
            dto.setIsActive(true);
            dto.setPermissions(List.of(permissionAId));
            roleService.create(dto);
        }

        PagedResponse<RoleResponse> page = roleService.list(1, 2, null, null, null, null);

        assertEquals(1, page.getMeta().getCurrent());
        assertEquals(2, page.getMeta().getPageSize());
        assertEquals(3, page.getMeta().getTotal());
        assertFalse(page.getResult().isEmpty());
    }

    @Test
    void deleteAdminRole_shouldThrow() {
        CreateRoleDto dto = new CreateRoleDto();
        dto.setName(RoleService.ADMIN_ROLE_NAME);
        dto.setIsActive(true);
        dto.setPermissions(List.of(permissionAId));
        roleService.create(dto);

        Long adminRoleId = roleRepository.findByRoleNameIgnoreCaseAndIsDeletedFalse(RoleService.ADMIN_ROLE_NAME)
                .map(r -> r.getRoleId())
                .orElseThrow();

        BadRequestException ex = assertThrows(BadRequestException.class, () -> roleService.delete(adminRoleId));
        assertEquals("Cannot delete admin role", ex.getMessage());
    }

    private Long createPermission(String name, String apiPath, String method) {
        Permission permission = new Permission();
        permission.setPermissionName(name);
        permission.setApiPath(apiPath);
        permission.setMethod(method);
        permission.setModule("TEST");
        permission.setCreatedBy(currentUserId);
        return permissionRepository.save(permission).getPermissionId();
    }
}
