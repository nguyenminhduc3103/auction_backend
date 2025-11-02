package vn.team9.auction_system.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.team9.auction_system.auth.dto.AuthResponse;
import vn.team9.auction_system.auth.dto.LoginRequest;
import vn.team9.auction_system.auth.dto.RegisterRequest;
import vn.team9.auction_system.auth.service.UserAuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserAuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        try {
            authService.verifyEmail(token);
            String html = """
            <html>
              <body style="font-family: Arial, sans-serif; background-color: #f4f7fa; text-align:center; padding:50px;">
                <div style="max-width:600px; margin:auto; background:white; border-radius:12px;
                            box-shadow:0 4px 10px rgba(0,0,0,0.05); padding:40px;">
                  <img src="https://github.com/TumRoyall/IT4409-BidSphere/blob/main/03_Development/auction-system-frontend/src/assets/logo.png?raw=true" style="height:60px; background-color:#0b2b4c; padding:8px; border-radius:8px;">
                  <h2 style="color:#0b2b4c; margin-top:24px;">🎉 Xác thực tài khoản thành công!</h2>
                  <p>Cảm ơn bạn đã xác thực email. Tài khoản của bạn đã được kích hoạt.</p>
                  <a href="https://1xbid.com/login" 
                     style="display:inline-block;margin-top:20px;padding:12px 24px;background-color:#0b2b4c;
                            color:white;border-radius:8px;text-decoration:none;font-weight:bold;">
                     Đăng nhập ngay
                  </a>
                  <p style="color:gray; font-size:13px; margin-top:30px;">© 2025 1xBid Team</p>
                </div>
              </body>
            </html>
        """;
            return ResponseEntity.ok().header("Content-Type", "text/html; charset=UTF-8").body(html);
        } catch (RuntimeException e) {
            String html = """
            <html>
              <body style="font-family: Arial, sans-serif; background-color: #f4f7fa; text-align:center; padding:50px;">
                <div style="max-width:600px; margin:auto; background:white; border-radius:12px;
                            box-shadow:0 4px 10px rgba(0,0,0,0.05); padding:40px;">
                  <img src="https://github.com/TumRoyall/IT4409-BidSphere/blob/main/03_Development/auction-system-frontend/src/assets/logo.png?raw=true" style="height:60px; background-color:#0b2b4c; padding:8px; border-radius:8px;">
                  <h2 style="color:red; margin-top:24px;">❌ Liên kết xác thực không hợp lệ hoặc đã hết hạn</h2>
                  <p>Vui lòng đăng ký lại hoặc yêu cầu gửi lại email xác thực.</p>
                  <a href="#" 
                     style="display:inline-block;margin-top:20px;padding:12px 24px;background-color:#0b2b4c;
                            color:white;border-radius:8px;text-decoration:none;font-weight:bold;">
                     Đăng ký lại
                  </a>
                  <p style="color:gray; font-size:13px; margin-top:30px;">© 2025 1xBid Team</p>
                </div>
              </body>
            </html>
        """;
            return ResponseEntity.badRequest().header("Content-Type", "text/html ; charset=UTF-8").body(html);
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(@RequestParam String email) {
        try {
            authService.resendVerification(email);
            return ResponseEntity.ok("✅ Email xác thực mới đã được gửi!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}