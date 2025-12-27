package vn.team9.auction_system.config.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import vn.team9.auction_system.auth.service.JwtService;
import vn.team9.auction_system.user.model.User;
import vn.team9.auction_system.user.repository.UserRepository;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketInterceptor implements ChannelInterceptor {
    
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        
        // Only process CONNECT commands
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            try {
                String token = getTokenFromHeaders(accessor);
                
                if (token == null || token.isEmpty()) {
                    log.warn("Missing JWT token in WebSocket connection");
                    throw new RuntimeException("Missing authorization token");
                }
                
                log.debug("Token received, length: {}", token.length());
                
                // Extract username first
                String username;
                try {
                    username = jwtService.extractUsername(token);
                    log.debug("Username extracted from token: {}", username);
                } catch (Exception e) {
                    log.warn("Failed to extract username from token: {}", e.getMessage());
                    throw new RuntimeException("Invalid authorization token: " + e.getMessage());
                }
                
                // Validate token with username
                if (!jwtService.isTokenValid(token, username)) {
                    log.warn("Token validation failed for username: {}", username);
                    throw new RuntimeException("Invalid or expired authorization token");
                }
                
                log.debug("Token validated successfully for user: {}", username);
                
                // Get user from database
                User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> {
                        log.warn("User not found in database: {}", username);
                        return new RuntimeException("User not found: " + username);
                    });
                
                // Set user as session attribute
                accessor.setUser(() -> String.valueOf(user.getUserId()));
                
                // Also set as header for later use
                accessor.setHeader("userId", String.valueOf(user.getUserId()));
                accessor.setHeader("username", username);
                
                log.info("✓ WebSocket connection established for user: {} (ID: {})", username, user.getUserId());
                
            } catch (Exception e) {
                log.error("✗ WebSocket authentication failed: {}", e.getMessage());
                throw new RuntimeException("Authentication failed: " + e.getMessage());
            }
        }
        
        return message;
    }

    /**
     * Extract JWT token from WebSocket STOMP headers
     * SockJS/STOMP client sends authentication headers in the CONNECT frame
     */
    private String getTokenFromHeaders(StompHeaderAccessor accessor) {
        // Try to get from X-Auth-Token header (primary method)
        String token = accessor.getFirstNativeHeader("X-Auth-Token");
        if (token != null && !token.isEmpty()) {
            log.debug("Token extracted from X-Auth-Token header");
            return token;
        }
        
        // Try to get token from Authorization header (Bearer token format)
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            log.debug("Token extracted from Authorization header");
            return authHeader.substring(7);
        }
        
        log.warn("No token found in WebSocket STOMP headers");
        return null;
    }
}
