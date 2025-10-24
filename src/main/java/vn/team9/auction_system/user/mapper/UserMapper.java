package vn.team9.auction_system.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.team9.auction_system.user.model.User;
import vn.team9.auction_system.common.dto.user.UserRequest;
import vn.team9.auction_system.common.dto.user.UserResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // UserRequest -> User entity
    User toEntity(UserRequest request);

    // User entity -> UserResponse
    @Mapping(source = "role.roleName", target = "role")
    UserResponse toResponse(User user);
}
