package vn.team9.auction_system.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String token) {
        String verifyLink = "http://localhost:8080/api/auth/verify?token=" + token;
        String subject = "🎉 Xác thực tài khoản của bạn - 1xBid";

        String htmlContent = """
            <html>
              <body style="font-family: Arial, sans-serif; background-color: #f4f7fa; padding: 0; margin: 0;">
                <div style="max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 12px; 
                            box-shadow: 0 4px 10px rgba(0,0,0,0.05); overflow: hidden;">
                  
                  <div style="background-color: #0b2b4c; padding: 24px 0; text-align: center;">
                    <img src="https://github.com/TumRoyall/IT4409-BidSphere/blob/main/03_Development/auction-system-frontend/src/assets/logo.png" alt="1xBid Logo" style="height: 50px;" />
                  </div>

                  <div style="padding: 24px 32px; color: #333;">
                    <h2 style="color: #0b2b4c;">Chào bạn,</h2>
                    <p>Cảm ơn bạn đã đăng ký tài khoản tại <b>1xBid</b> – sàn đấu giá trực tuyến thông minh.</p>
                    <p>Chỉ còn một bước nữa là bạn có thể tham gia các phiên đấu giá hấp dẫn!</p>
                    <p>Hãy xác thực tài khoản của bạn bằng cách bấm vào nút dưới đây:</p>

                    <div style="text-align: center; margin: 28px 0;">
                      <a href="%s" 
                        style="background-color:#0b2b4c;color:white;padding:14px 28px;border-radius:8px;
                               text-decoration:none;font-weight:bold;display:inline-block;">
                        Kích hoạt tài khoản
                      </a>
                    </div>

                    <p>Nếu bạn không thực hiện đăng ký này, vui lòng bỏ qua email này.</p>
                    <p style="color:gray; font-size: 13px;">
                      Liên kết xác thực chỉ có hiệu lực trong 15 phút kể từ khi bạn nhận được email này.
                    </p>
                  </div>

                  <div style="background-color: #f1f5f9; padding: 16px 0; text-align: center; font-size: 13px; color: #555;">
                    <p>© 2025 1xBid - Nền tảng đấu giá trực tuyến hàng đầu Việt Nam</p>
                    <p>
                      <a href="https://1xbid.com" style="color:#0b2b4c;text-decoration:none;">Trang chủ</a> | 
                      <a href="mailto:support@1xbid.com" style="color:#0b2b4c;text-decoration:none;">Hỗ trợ</a>
                    </p>
                  </div>
                </div>
              </body>
            </html>
            """.formatted(verifyLink);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);

            System.out.println("✅ Đã gửi email xác thực HTML tới " + to);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi gửi email xác thực: " + e.getMessage());
        }
    }
}