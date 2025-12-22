package vn.team9.auction_system.auth.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import vn.team9.auction_system.common.dto.role.CreateRoleDto;
import vn.team9.auction_system.common.dto.role.RoleResponse;
import vn.team9.auction_system.common.dto.role.UpdateRoleDto;
import vn.team9.auction_system.user.model.Role;

@Mapper(componentModel = "spring", uses = PermissionMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {

    @Mapping(target = "roleId", ignore = true)
    @Mapping(source = "name", target = "roleName")
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    Role toEntity(CreateRoleDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "name", target = "roleName")
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateEntity(@MappingTarget Role role, UpdateRoleDto dto);

    @Mapping(source = "roleId", target = "id")
    @Mapping(source = "roleName", target = "name")
    RoleResponse toResponse(Role role);
}
