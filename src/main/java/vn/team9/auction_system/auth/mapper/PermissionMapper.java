package vn.team9.auction_system.auth.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import vn.team9.auction_system.auth.model.Permission;
import vn.team9.auction_system.common.dto.permission.CreatePermissionDto;
import vn.team9.auction_system.common.dto.permission.PermissionResponse;
import vn.team9.auction_system.common.dto.permission.UpdatePermissionDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PermissionMapper {

    @Mapping(source = "name", target = "permissionName")
    @Mapping(target = "permissionId", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    Permission toEntity(CreatePermissionDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "name", target = "permissionName")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateEntity(@MappingTarget Permission permission, UpdatePermissionDto dto);

    @Mapping(source = "permissionId", target = "id")
    @Mapping(source = "permissionName", target = "name")
    PermissionResponse toResponse(Permission permission);
}
