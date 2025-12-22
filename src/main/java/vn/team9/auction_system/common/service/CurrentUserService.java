package vn.team9.auction_system.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import vn.team9.auction_system.common.exception.BadRequestException;
import vn.team9.auction_system.user.model.User;
import vn.team9.auction_system.user.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("Vui lòng đăng nhập");
        }

        String email;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else if (principal instanceof String s) {
            email = s;
        } else {
            throw new BadRequestException("Không thể xác định người dùng hiện tại");
        }

        return userRepository.findByEmail(email)
                .map(User::getUserId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy người dùng với email: " + email));
    }
}
