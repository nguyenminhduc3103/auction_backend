package vn.team9.auction_system.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.team9.auction_system.auth.mapper.PermissionMapper;
import vn.team9.auction_system.auth.model.Permission;
import vn.team9.auction_system.auth.repository.PermissionRepository;
import vn.team9.auction_system.auth.specification.PermissionSpecification;
import vn.team9.auction_system.common.dto.common.CreateResponse;
import vn.team9.auction_system.common.dto.pagination.PageMeta;
import vn.team9.auction_system.common.dto.pagination.PagedResponse;
import vn.team9.auction_system.common.dto.permission.CreatePermissionDto;
import vn.team9.auction_system.common.dto.permission.PermissionResponse;
import vn.team9.auction_system.common.dto.permission.UpdatePermissionDto;
import vn.team9.auction_system.common.exception.BadRequestException;
import vn.team9.auction_system.common.exception.NotFoundException;
import vn.team9.auction_system.common.service.CurrentUserService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final CurrentUserService currentUserService;

    public CreateResponse create(CreatePermissionDto dto) {
        if (permissionRepository.existsByApiPathIgnoreCaseAndMethodIgnoreCaseAndIsDeletedFalse(dto.getApiPath(), dto.getMethod())) {
            throw new BadRequestException("Permission already exists");
        }
        if (permissionRepository.existsByPermissionNameIgnoreCaseAndIsDeletedFalse(dto.getName())) {
            throw new BadRequestException("Permission already exists");
        }

        Permission permission = permissionMapper.toEntity(dto);
        permission.setMethod(dto.getMethod().toUpperCase());
        permission.setCreatedBy(currentUserService.getCurrentUserId());

        Permission saved = permissionRepository.save(permission);
        return new CreateResponse(saved.getPermissionId(), saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public PagedResponse<PermissionResponse> list(int current, int pageSize, String name, String module, String method, String sortBy, String sortDirection) {
        int pageIndex = Math.max(current - 1, 0);
        int size = pageSize > 0 ? pageSize : 10;
        String sortField = StringUtils.hasText(sortBy) ? sortBy : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(pageIndex, size, Sort.by(direction, sortField));

        Page<Permission> page = permissionRepository.findAll(
                PermissionSpecification.filter(name, module, method),
                pageRequest
        );

        List<PermissionResponse> result = page.getContent().stream()
                .map(permissionMapper::toResponse)
                .toList();

        PageMeta meta = new PageMeta(page.getNumber() + 1, page.getSize(), page.getTotalPages(), page.getTotalElements());
        return new PagedResponse<>(meta, result);
    }

    @Transactional(readOnly = true)
    public PermissionResponse getOne(Long id) {
        validateId(id);
        Permission permission = permissionRepository.findByPermissionIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Permission not found"));
        return permissionMapper.toResponse(permission);
    }

    public PermissionResponse update(Long id, UpdatePermissionDto dto) {
        validateId(id);
        Permission permission = permissionRepository.findByPermissionIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Permission not found"));

        if (StringUtils.hasText(dto.getApiPath()) || StringUtils.hasText(dto.getMethod())) {
            String apiPath = StringUtils.hasText(dto.getApiPath()) ? dto.getApiPath() : permission.getApiPath();
            String method = StringUtils.hasText(dto.getMethod()) ? dto.getMethod() : permission.getMethod();
            boolean duplicate = permissionRepository.existsByApiPathIgnoreCaseAndMethodIgnoreCaseAndIsDeletedFalse(apiPath, method)
                    && !(apiPath.equalsIgnoreCase(permission.getApiPath()) && method.equalsIgnoreCase(permission.getMethod()));
            if (duplicate) {
                throw new BadRequestException("Permission already exists");
            }
        }
        if (StringUtils.hasText(dto.getName()) && permissionRepository.existsByPermissionNameIgnoreCaseAndIsDeletedFalse(dto.getName())
                && !dto.getName().equalsIgnoreCase(permission.getPermissionName())) {
            throw new BadRequestException("Permission already exists");
        }

        permissionMapper.updateEntity(permission, dto);
        if (StringUtils.hasText(dto.getMethod())) {
            permission.setMethod(dto.getMethod().toUpperCase());
        }
        permission.setUpdatedBy(currentUserService.getCurrentUserId());

        Permission saved = permissionRepository.save(permission);
        return permissionMapper.toResponse(saved);
    }

    public void delete(Long id) {
        validateId(id);
        Permission permission = permissionRepository.findByPermissionIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Permission not found"));
        permission.markDeleted(currentUserService.getCurrentUserId());
        permissionRepository.save(permission);
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid id");
        }
    }
}
