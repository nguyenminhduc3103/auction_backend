package vn.team9.auction_system.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

    // ✅ Cập nhật user theo email (dành cho /me)
    public UserResponse updateByEmail(String email, UpdateUserDTO request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getGender() != null) user.setGender(request.getGender());

        userRepository.save(user);
        return userMapper.toResponse(user);
    }
}
