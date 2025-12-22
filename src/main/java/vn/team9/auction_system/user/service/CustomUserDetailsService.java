package vn.team9.auction_system.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.team9.auction_system.user.model.User;
import vn.team9.auction_system.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user với email: " + email));

        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRole() != null && user.getRole().getRoleName() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName().toUpperCase()));

            if (user.getRole().getPermissions() != null) {
            user.getRole().getPermissions().stream()
                .filter(permission -> permission.getIsDeleted() == null || !permission.getIsDeleted())
                .map(permission -> permission.getMethod().toUpperCase() + ":" + permission.getApiPath())
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);
            }
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
            .authorities(authorities)
                .build();
    }
}
