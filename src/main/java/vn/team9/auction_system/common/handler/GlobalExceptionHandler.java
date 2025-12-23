package vn.team9.auction_system.common.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Lỗi nghiệp vụ (sai mật khẩu, không đủ tiền, ...)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", 400);
        body.put("error", "Business Error");
        body.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    // RuntimeException khác (BUG)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", 500);
        body.put("error", "Internal Server Error");
        body.put("message", "Hệ thống đang gặp sự cố");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // Lỗi không lường trước
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOtherExceptions(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", 500);
        body.put("error", "Internal Server Error");
        body.put("message", "Lỗi hệ thống");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}

