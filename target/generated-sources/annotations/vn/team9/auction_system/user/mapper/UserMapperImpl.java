package vn.team9.auction_system.user.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import vn.team9.auction_system.common.dto.user.UserResponse;
import vn.team9.auction_system.user.model.Role;
import vn.team9.auction_system.user.model.User;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-29T20:07:28+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 22.0.2 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse userResponse = new UserResponse();

        userResponse.setRoleId( userRoleRoleId( user ) );
        userResponse.setRoleName( userRoleRoleName( user ) );
        userResponse.setUserId( user.getUserId() );
        userResponse.setFullName( user.getFullName() );
        userResponse.setUsername( user.getUsername() );
        userResponse.setEmail( user.getEmail() );
        userResponse.setPhone( user.getPhone() );
        userResponse.setGender( user.getGender() );
        userResponse.setBalance( user.getBalance() );
        userResponse.setStatus( user.getStatus() );
        userResponse.setCreatedAt( user.getCreatedAt() );
        userResponse.setVerifiedAt( user.getVerifiedAt() );
        userResponse.setAvatarUrl( user.getAvatarUrl() );
        userResponse.setBannedUntil( user.getBannedUntil() );

        return userResponse;
    }

    private Long userRoleRoleId(User user) {
        if ( user == null ) {
            return null;
        }
        Role role = user.getRole();
        if ( role == null ) {
            return null;
        }
        Long roleId = role.getRoleId();
        if ( roleId == null ) {
            return null;
        }
        return roleId;
    }

    private String userRoleRoleName(User user) {
        if ( user == null ) {
            return null;
        }
        Role role = user.getRole();
        if ( role == null ) {
            return null;
        }
        String roleName = role.getRoleName();
        if ( roleName == null ) {
            return null;
        }
        return roleName;
    }
}
