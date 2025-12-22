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
import vn.team9.auction_system.common.dto.pagination.PagedResponse;
import vn.team9.auction_system.common.dto.permission.CreatePermissionDto;
import vn.team9.auction_system.common.dto.permission.PermissionResponse;
import vn.team9.auction_system.common.exception.BadRequestException;
import vn.team9.auction_system.user.model.User;
import vn.team9.auction_system.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PermissionServiceTest {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("creator@test.com");
        user.setUsername("creator");
        user.setPasswordHash("password");
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), null, List.of())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createPermission_success() {
        CreatePermissionDto dto = new CreatePermissionDto();
        dto.setName("View products");
        dto.setApiPath("/api/products");
        dto.setMethod("GET");
        dto.setModule("PRODUCT");

        var created = permissionService.create(dto);

        assertNotNull(created.getId());
        assertNotNull(created.getCreatedAt());
    }

    @Test
    void createPermission_duplicateApiMethod_shouldThrow() {
        CreatePermissionDto dto = new CreatePermissionDto();
        dto.setName("Manage products");
        dto.setApiPath("/api/products");
        dto.setMethod("POST");
        dto.setModule("PRODUCT");

        permissionService.create(dto);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> permissionService.create(dto));
        assertEquals("Permission already exists", ex.getMessage());
    }

    @Test
    void listPermissions_returnsPagedMeta() {
        for (int i = 0; i < 3; i++) {
            CreatePermissionDto dto = new CreatePermissionDto();
            dto.setName("Perm" + i);
            dto.setApiPath("/api/p" + i);
            dto.setMethod("GET");
            dto.setModule("TEST");
            permissionService.create(dto);
        }

        PagedResponse<PermissionResponse> page = permissionService.list(1, 2, null, null, null, null, null);

        assertEquals(1, page.getMeta().getCurrent());
        assertEquals(2, page.getMeta().getPageSize());
        assertEquals(3, page.getMeta().getTotal());
        assertFalse(page.getResult().isEmpty());
    }
}
