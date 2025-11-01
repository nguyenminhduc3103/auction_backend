package vn.team9.auction_system.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * 🔐 JwtService: Quản lý sinh, xác minh và giải mã JSON Web Token (JWT)
 */
@Service
public class JwtService {

    // ==============================
    // ⚙️ Cấu hình token
    // ==============================
    // Secret key nên lưu trong application.properties hoặc môi trường (ENV)
    // Ví dụ: jwt.secret=Base64EncodedSecretKey
    private static final String SECRET_KEY =
            "YXVkY3Rpb24xMEJpZFNwaGVyZVN1cGVyU2VjcmV0S2V5MTIzNDU2Nzg5MA==";

    // Thời gian sống của token (ms): 1h = 3_600_000 ms
    private static final long EXPIRATION_TIME = 1000 * 60 * 60;

    // ==============================
    // ⚡ Sinh token
    // ==============================
    public String generateToken(String subject) {
        return generateToken(Map.of(), subject);
    }

    public String generateToken(Map<String, Object> extraClaims, String subject) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)         // thường là email hoặc userId
                .setIssuedAt(now)            // thời điểm phát hành
                .setExpiration(expiry)       // thời điểm hết hạn
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ==============================
    // 🔍 Xác minh token
    // ==============================
    public boolean isTokenValid(String token, String expectedEmail) {
        final String actualEmail = extractClaim(token, Claims::getSubject);
        return (actualEmail.equals(expectedEmail)) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ==============================
    // 🧩 Giải mã claims
    // ==============================
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token đã hết hạn!");
        } catch (JwtException e) {
            throw new RuntimeException("Token không hợp lệ!");
        }
    }

    // ==============================
    // 🔑 Khóa ký JWT
    // ==============================
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
