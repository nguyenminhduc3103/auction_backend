package vn.team9.auction_system.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.team9.auction_system.user.model.User;
import vn.team9.auction_system.common.dto.user.UserResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "role.roleId", target = "roleId")
    @Mapping(source = "role.roleName", target = "roleName")
    UserResponse toResponse(User user);
}
