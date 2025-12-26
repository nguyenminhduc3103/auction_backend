package vn.team9.auction_system.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import vn.team9.auction_system.auction.service.AuctionServiceImpl;
import vn.team9.auction_system.common.dto.auction.AuctionResponse;
import vn.team9.auction_system.common.dto.user.ChangePasswordRequest;
import vn.team9.auction_system.common.dto.user.UserResponse;
import vn.team9.auction_system.user.service.UserService;
import vn.team9.auction_system.common.dto.user.UpdateUserDTO;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuctionServiceImpl auctionService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.getByEmail(email));
    }

    // Get public profile of user by ID (from seller_profile branch)
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getPublicProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getPublicProfile(id));
    }

    // Update personal information (from main)
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UpdateUserDTO request) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.updateByEmail(email, request));
    }

    @PatchMapping("/change-password")
    public ResponseEntity<?> changePassword(Authentication authentication, @RequestBody ChangePasswordRequest req) {
        String email = authentication.getName();
        userService.changePasswordByEmail(email, req);
        return ResponseEntity.ok("Password changed successfully");
    }

    @PutMapping("/me/avatar")
    public ResponseEntity<?> updateAvatar(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        try {
            // Get current user via email
            String email = authentication.getName();
            UserResponse currentUser = userService.getByEmail(email);

            String filename = "ID_" + currentUser.getUserId() + "_" + currentUser.getUsername() + ".png";

            // Create directory if not exists
            Path uploadDir = Paths.get("src/main/resources/static/avatars/users/");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Write file (overwrite if exists)
            Path filePath = uploadDir.resolve(filename);
            Files.write(filePath, file.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // Update avatar_url in DB
            String relativeUrl = "/avatars/users/" + filename;
            userService.updateAvatarUrl(currentUser.getUserId(), relativeUrl);

            return ResponseEntity.ok(Map.of("avatarUrl", relativeUrl));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Unable to update avatar"));
        }
    }

    @GetMapping("/{userId}/auctions/participating")
    public ResponseEntity<?> getParticipatingOpenAuctions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "endTime,asc") String sort
    ) {
        return ResponseEntity.ok(
                auctionService.getParticipatingOpenAuctions(
                        userId,
                        page,
                        size,
                        sort
                )
        );
    }
}