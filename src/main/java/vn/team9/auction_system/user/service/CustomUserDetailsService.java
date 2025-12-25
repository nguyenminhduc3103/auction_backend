package vn.team9.auction_system.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.team9.auction_system.auth.model.Permission;
import vn.team9.auction_system.auth.repository.PermissionRepository;
import vn.team9.auction_system.user.model.User;
import vn.team9.auction_system.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user với email: " + email));

        List<GrantedAuthority> authorities = new ArrayList<>();

        if (user.getRole() != null && user.getRole().getRoleName() != null) {
            // Add ROLE authority
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName().toUpperCase()));

            // Load permissions via native query through rolepermission join table
            try {
                Long roleId = user.getRole().getRoleId();
                log.info("Loading permissions for roleId: {}", roleId);

                List<Permission> permissions = permissionRepository.findByRoleId(roleId);
                log.info("Found {} permissions for role {}", permissions.size(), user.getRole().getRoleName());

                permissions.stream()
                        .filter(permission -> permission.getIsDeleted() == null || !permission.getIsDeleted())
                        .map(permission -> {
                            String authority = permission.getMethod().toUpperCase() + ":" + permission.getApiPath();
                            log.info("  -> Adding authority: {}", authority);
                            return authority;
                        })
                        .map(SimpleGrantedAuthority::new)
                        .forEach(authorities::add);

            } catch (Exception e) {
                log.error("Could not load permissions for user {}: {}", email, e.getMessage(), e);
            }
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .build();
    }
}
