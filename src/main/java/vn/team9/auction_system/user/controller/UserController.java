package vn.team9.auction_system.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.team9.auction_system.auction.service.AuctionServiceImpl;
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

    @Value("${avatar.upload-dir:uploads/avatars/}")
    private String avatarBaseDir;

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('GET:/api/users/me')")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.getByEmail(email));
    }

    // Get public profile of user by ID (from seller_profile branch)
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('GET:/api/users/{id}')")
    public ResponseEntity<UserResponse> getPublicProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getPublicProfile(id));
    }

    // Update personal information
    @PutMapping("/me")
    @PreAuthorize("hasAuthority('PUT:/api/users/me')")
    public ResponseEntity<UserResponse> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UpdateUserDTO request) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.updateByEmail(email, request));
    }

    @PatchMapping("/change-password")
    @PreAuthorize("hasAuthority('PATCH:/api/users/change-password')")
    public ResponseEntity<?> changePassword(Authentication authentication, @RequestBody ChangePasswordRequest req) {
        String email = authentication.getName();
        userService.changePasswordByEmail(email, req);
        return ResponseEntity.ok("Password changed successfully");
    }

    @PutMapping("/me/avatar")
    @PreAuthorize("hasAuthority('PUT:/api/users/me/avatar')")
    public ResponseEntity<?> updateAvatar(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        try {
            // Get current user via email
            String email = authentication.getName();
            UserResponse currentUser = userService.getByEmail(email);

            String filename = "ID_" + currentUser.getUserId() + "_" + currentUser.getUsername() + ".png";

            // Create directory if not exists (use external uploads folder)
            Path uploadDir = Paths.get(avatarBaseDir, "users").toAbsolutePath().normalize();
            log.info("[Avatar] Upload base dir: {}", uploadDir);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                log.info("[Avatar] Created directory: {}", uploadDir);
            }

            // Write file (overwrite if exists)
            Path filePath = uploadDir.resolve(filename);
            log.info("[Avatar] Writing file: {} (size={} bytes)", filePath, file.getSize());
            Files.write(filePath, file.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // Update avatar_url in DB (relative path served by resource handler)
            String relativeUrl = "/avatars/users/" + filename;
            userService.updateAvatarUrl(currentUser.getUserId(), relativeUrl);
            log.info("[Avatar] DB updated for userId={} url={}", currentUser.getUserId(), relativeUrl);

            return ResponseEntity.ok(Map.of("avatarUrl", relativeUrl));

        } catch (Exception e) {
            log.error("[Avatar] Failed to update avatar: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Unable to update avatar"));
        }
    }

    @GetMapping("/{userId}/auctions/participating")
    public ResponseEntity<?> getParticipatingOpenAuctions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "endTime,asc") String sort) {
        return ResponseEntity.ok(
                auctionService.getParticipatingOpenAuctions(
                        userId,
                        page,
                        size,
                        sort));
    }

    // Upgrade BIDDER to SELLER role
    @PutMapping("/upgrade-to-seller")
    public ResponseEntity<?> upgradeToSeller(Authentication authentication) {
        String email = authentication.getName();
        try {
            UserResponse updatedUser = userService.upgradeToSeller(email);
            return ResponseEntity.ok(Map.of(
                    "message", "Successfully upgraded to SELLER",
                    "user", updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage()));
        }
    }
}