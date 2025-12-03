package vn.team9.auction_system.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.team9.auction_system.common.dto.user.ChangePasswordRequest;
import vn.team9.auction_system.common.dto.user.UpdateUserDTO;
import vn.team9.auction_system.common.dto.user.UserResponse;
import vn.team9.auction_system.user.mapper.UserMapper;
import vn.team9.auction_system.user.model.User;
import vn.team9.auction_system.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));
        return userMapper.toResponse(user);
    }

    // Lấy public profile của user theo ID
    @Transactional
    public UserResponse getPublicProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));
        return userMapper.toResponse(user);
    }

    // Cập nhật user theo email (dành cho /me)
    public UserResponse updateByEmail(String email, UpdateUserDTO request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getGender() != null) user.setGender(request.getGender());

        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void changePasswordByEmail(String email, ChangePasswordRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng");
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    public void updateAvatarUrl(Long userId, String avatarUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
    }


}
