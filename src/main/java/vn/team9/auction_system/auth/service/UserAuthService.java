package vn.team9.auction_system.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.team9.auction_system.auth.dto.*;
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

    // ==========================
    // 🔐 REGISTER USER
    // ==========================
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
                System.out.println("📨 Đã gửi lại email xác thực cho " + existingUser.getEmail());

                return AuthResponse.builder()
                        .email(existingUser.getEmail())
                        .fullName(existingUser.getFullName())
                        .username(existingUser.getUsername())
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

        System.out.println("📧 Sending verification email to: " + user.getEmail());
        emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());

        return AuthResponse.builder()
                .accessToken(null)
                .tokenType("Bearer")
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .build();
    }


    // ==========================
    // 🧾 VERIFY EMAIL
    // ==========================
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

    // ==========================
    // 🔑 LOGIN USER
    // ==========================
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Sai mật khẩu.");
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new RuntimeException("Tài khoản chưa được xác thực hoặc đã bị khoá.");
        }

        String token = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .build();
    }

    // ==========================
    // 👤 GET CURRENT USER
    // ==========================
    public User getCurrentUser(String token) {
        String email = jwtService.extractUsername(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng."));
    }
}
