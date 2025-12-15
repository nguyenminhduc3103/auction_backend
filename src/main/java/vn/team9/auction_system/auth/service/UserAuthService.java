package vn.team9.auction_system.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.team9.auction_system.common.dto.auth.AuthResponse;
import vn.team9.auction_system.common.dto.auth.LoginRequest;
import vn.team9.auction_system.common.dto.auth.RegisterRequest;
import vn.team9.auction_system.user.model.User;
import vn.team9.auction_system.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    // REGISTER USER
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Tìm user theo email
        Optional<User> existingOpt = userRepository.findByEmail(request.getEmail());

        // Nếu đã có user
        if (existingOpt.isPresent()) {
            User existingUser = existingOpt.get();

            // Nếu user chưa xác thực (PENDING) → gửi lại mail xác thực
            if ("PENDING".equalsIgnoreCase(existingUser.getStatus())) {
                existingUser.setVerificationToken(UUID.randomUUID().toString());
                existingUser.setVerificationTokenExpiry(LocalDateTime.now().plusMinutes(15));
                userRepository.save(existingUser);

                emailService.sendVerificationEmail(existingUser.getEmail(), existingUser.getVerificationToken());
                System.out.println("Đã gửi lại email xác thực cho " + existingUser.getEmail());

                return AuthResponse.builder()
                        .userId(existingUser.getUserId())
                        .gender(existingUser.getGender())
                        .email(existingUser.getEmail())
                        .fullName(existingUser.getFullName())
                        .username(existingUser.getUsername())
                        .status(existingUser.getStatus())
                        .tokenType("Bearer")
                        .accessToken(null)
                        .build();
            }

            // Nếu user đã xác thực → không cho đăng ký lại
            if ("ACTIVE".equalsIgnoreCase(existingUser.getStatus())) {
                throw new RuntimeException("Email đã được đăng ký và xác thực. Vui lòng đăng nhập.");
            }
        }

        // Nếu email chưa tồn tại → tạo user mới
        User user = new User();
        user.setGender(request.getGender());
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus("PENDING");
        user.setCreatedAt(LocalDateTime.now());
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationTokenExpiry(LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);

        System.out.println("Sending verification email to: " + user.getEmail());
        emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());

        return AuthResponse.builder()
                .userId(user.getUserId())
                .accessToken(null)
                .tokenType("Bearer")
                .username(user.getUsername())
                .fullName(user.getFullName())
                .gender(user.getGender())
                .status(user.getStatus())
                .email(user.getEmail())
                .build();
    }


    // VERIFY EMAIL
    @Transactional
    public String verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Token xác thực không hợp lệ."));

        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Liên kết xác thực đã hết hạn. Vui lòng đăng ký lại hoặc yêu cầu gửi lại email xác thực.");
        }

        user.setStatus("ACTIVE");
        user.setVerificationToken(null);
        user.setVerifiedAt(LocalDateTime.now());
        userRepository.save(user);

        return "Xác thực tài khoản thành công.";
    }

    //  LOGIN USER
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        // Kiểm tra ban tạm thời
        if ("BANNED".equalsIgnoreCase(user.getStatus())) {
            if (user.getBannedUntil() != null) {
                LocalDateTime now = LocalDateTime.now();
                if (user.getBannedUntil().isAfter(now)) {
                    throw new RuntimeException(
                            "Tài khoản bị khóa đến " + user.getBannedUntil() +
                                    (user.getBanReason() != null ? " | Lý do: " + user.getBanReason() : "")
                    );
                } else {
                    // Hết hạn ban → mở lại
                    user.setStatus("ACTIVE");
                    user.setBannedUntil(null);
                    user.setBanReason(null);
                    userRepository.save(user);
                }
            } else {
                // Ban vĩnh viễn (không có bannedUntil)
                throw new RuntimeException(
                        "Tài khoản của bạn đã bị khóa vĩnh viễn" +
                                (user.getBanReason() != null ? " | Lý do: " + user.getBanReason() : "")
                );
            }
        }
        System.out.println("🟢 Login request: email=" + request.getEmail() + ", pass=" + request.getPassword());
        if ("PENDING".equalsIgnoreCase(user.getStatus())) {
            throw new RuntimeException("Vui lòng xác thực email trước khi đăng nhập.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu không chính xác!");
        }

        // ACTIVE -> login bình thường
        String token = jwtService.generateToken(user.getEmail());
        return AuthResponse.builder()
                .userId(user.getUserId())
                .accessToken(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .status(user.getStatus())
                .gender(user.getGender())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    // RESEND EMAIL FOR PENDING ACCOUNT
    @Transactional
    public void resendVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));

        if (!"PENDING".equalsIgnoreCase(user.getStatus())) {
            throw new RuntimeException("Tài khoản này đã được xác thực hoặc không hợp lệ.");
        }

        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());
        System.out.println("📨 Đã gửi lại email xác thực cho " + user.getEmail());
    }

}
