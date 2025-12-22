package vn.team9.auction_system.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.team9.auction_system.auth.mapper.RoleMapper;
import vn.team9.auction_system.auth.model.Permission;
import vn.team9.auction_system.auth.repository.PermissionRepository;
import vn.team9.auction_system.auth.repository.RoleRepository;
import vn.team9.auction_system.auth.specification.RoleSpecification;
import vn.team9.auction_system.common.dto.common.CreateResponse;
import vn.team9.auction_system.common.dto.pagination.PageMeta;
import vn.team9.auction_system.common.dto.pagination.PagedResponse;
import vn.team9.auction_system.common.dto.role.CreateRoleDto;
import vn.team9.auction_system.common.dto.role.RoleResponse;
import vn.team9.auction_system.common.dto.role.UpdateRoleDto;
import vn.team9.auction_system.common.exception.BadRequestException;
import vn.team9.auction_system.common.exception.NotFoundException;
import vn.team9.auction_system.common.service.CurrentUserService;
import vn.team9.auction_system.user.model.Role;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

    public static final String ADMIN_ROLE_NAME = "ADMIN";

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;
    private final CurrentUserService currentUserService;

    public CreateResponse create(CreateRoleDto dto) {
        if (roleRepository.existsByRoleNameIgnoreCaseAndIsDeletedFalse(dto.getName())) {
            throw new BadRequestException("Role already exists");
        }

        Role role = roleMapper.toEntity(dto);
        role.setCreatedBy(currentUserService.getCurrentUserId());
        role.setPermissions(resolvePermissions(dto.getPermissions()));

        Role saved = roleRepository.save(role);
        return new CreateResponse(saved.getRoleId(), saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public PagedResponse<RoleResponse> list(int current, int pageSize, String name, Boolean isActive, String sortBy, String sortDirection) {
        int pageIndex = Math.max(current - 1, 0);
        int size = pageSize > 0 ? pageSize : 10;
        String sortField = StringUtils.hasText(sortBy) ? sortBy : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(pageIndex, size, Sort.by(direction, sortField));

        Page<Role> page = roleRepository.findAll(RoleSpecification.filter(name, isActive), pageRequest);

        List<RoleResponse> result = page.getContent().stream()
                .map(roleMapper::toResponse)
                .toList();

        PageMeta meta = new PageMeta(page.getNumber() + 1, page.getSize(), page.getTotalPages(), page.getTotalElements());
        return new PagedResponse<>(meta, result);
    }

    @Transactional(readOnly = true)
    public RoleResponse getOne(Long id) {
        validateId(id);
        Role role = roleRepository.findByRoleIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Role not found"));
        return roleMapper.toResponse(role);
    }

    public RoleResponse update(Long id, UpdateRoleDto dto) {
        validateId(id);
        Role role = roleRepository.findByRoleIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Role not found"));

        if (StringUtils.hasText(dto.getName())
                && roleRepository.existsByRoleNameIgnoreCaseAndIsDeletedFalse(dto.getName())
                && !dto.getName().equalsIgnoreCase(role.getRoleName())) {
            throw new BadRequestException("Role already exists");
        }

        if (dto.getPermissions() != null) {
            role.setPermissions(resolvePermissions(dto.getPermissions()));
        }

        roleMapper.updateEntity(role, dto);
        role.setUpdatedBy(currentUserService.getCurrentUserId());

        Role saved = roleRepository.save(role);
        return roleMapper.toResponse(saved);
    }

    public void delete(Long id) {
        validateId(id);
        Role role = roleRepository.findByRoleIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Role not found"));

        if (role.getRoleName() != null && role.getRoleName().equalsIgnoreCase(ADMIN_ROLE_NAME)) {
            throw new BadRequestException("Cannot delete admin role");
        }

        role.markDeleted(currentUserService.getCurrentUserId());
        roleRepository.save(role);
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid id");
        }
    }

    private Set<Permission> resolvePermissions(List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            throw new BadRequestException("permissions cannot be empty");
        }
        Set<Permission> permissions = new HashSet<>();
        for (Long permissionId : permissionIds) {
            if (permissionId == null || permissionId <= 0) {
                throw new BadRequestException("Invalid id");
            }
            Permission permission = permissionRepository.findByPermissionIdAndIsDeletedFalse(permissionId)
                    .orElseThrow(() -> new NotFoundException("Permission not found"));
            permissions.add(permission);
        }
        return permissions;
    }
}
