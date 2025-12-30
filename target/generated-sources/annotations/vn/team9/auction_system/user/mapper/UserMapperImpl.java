package vn.team9.auction_system.user.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import vn.team9.auction_system.common.dto.user.UserResponse;
import vn.team9.auction_system.user.model.Role;
import vn.team9.auction_system.user.model.User;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-30T23:18:09+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
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
        userResponse.setAvatarUrl( user.getAvatarUrl() );
        userResponse.setBalance( user.getBalance() );
        userResponse.setBannedUntil( user.getBannedUntil() );
        userResponse.setCreatedAt( user.getCreatedAt() );
        userResponse.setEmail( user.getEmail() );
        userResponse.setFullName( user.getFullName() );
        userResponse.setGender( user.getGender() );
        userResponse.setPhone( user.getPhone() );
        userResponse.setStatus( user.getStatus() );
        userResponse.setUserId( user.getUserId() );
        userResponse.setUsername( user.getUsername() );
        userResponse.setVerifiedAt( user.getVerifiedAt() );

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
