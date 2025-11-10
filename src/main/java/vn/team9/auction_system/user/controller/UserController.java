package vn.team9.auction_system.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import vn.team9.auction_system.common.dto.user.UserResponse;
import vn.team9.auction_system.user.service.UserService;
import vn.team9.auction_system.common.dto.user.UpdateUserDTO;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //Lấy thông tin của chính mình
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.getByEmail(email));
    }

    //Cập nhật thông tin của chính mình
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UpdateUserDTO request
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.updateByEmail(email, request));
    }
}
