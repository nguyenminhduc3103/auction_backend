package vn.team9.auction_system.user.service;

import vn.team9.auction_system.common.dto.admin.BanUserRequest;
import vn.team9.auction_system.common.dto.user.UserRequest;
import vn.team9.auction_system.common.dto.user.UserResponse;
import vn.team9.auction_system.common.service.IUserService;
import vn.team9.auction_system.user.model.User;
import vn.team9.auction_system.user.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // update các trường
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setGender(request.getGender());
        user.setStatus(request.getStatus());

        userRepository.save(user);

        return mapToResponse(user);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    @Override
    public UserResponse banUser(Long id, BanUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus("BANNED");
        user.setBanReason(request.getReason());
        user.setBannedUntil(request.getBannedUntil());
        userRepository.save(user);
        return mapToResponse(user);
    }

    @Override
    public UserResponse unbanUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Gỡ trạng thái cấm
        user.setStatus("ACTIVE");
        user.setBanReason(null);
        user.setBannedUntil(null);

        userRepository.save(user);
        return mapToResponse(user);
    }

    // mapping entity -> response
    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getUserId());
        response.setFullName(user.getFullName());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setGender(user.getGender());
        response.setStatus(user.getStatus());
        response.setBalance(user.getBalance());
        response.setCreatedAt(user.getCreatedAt());
        response.setVerifiedAt(user.getVerifiedAt());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setReason(user.getBanReason());
        response.setBannedUntil(user.getBannedUntil());
        return response;
    }

    @Override
    public UserResponse register(UserRequest request) {
        throw new UnsupportedOperationException("Register not supported in AdminServiceImpl");
    }
}
