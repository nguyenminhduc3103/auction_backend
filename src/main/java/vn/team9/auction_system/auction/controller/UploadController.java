package vn.team9.auction_system.auction.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "http://localhost:5173") // cho phép frontend truy cập
@RequiredArgsConstructor
public class UploadController {

    private final Cloudinary cloudinary;

    @PostMapping
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", "auction_images")
                );

                String secureUrl = Objects.toString(uploadResult.get("secure_url"), null);

                Map<String, Object> response = new HashMap<>();
                response.put("image_url", secureUrl);
                response.put("imageUrl", secureUrl);
                response.put("secure_url", secureUrl);
                response.put("secureUrl", secureUrl);
                response.put("public_id", uploadResult.get("public_id"));
                return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Upload thất bại: " + e.getMessage());
        }
    }

    @PostMapping("/multiple")
    public ResponseEntity<?> uploadMultipleImages(@RequestParam("files") MultipartFile[] files) {
        try {
            List<Map<String, Object>> uploadedImages = new ArrayList<>();

            for (MultipartFile file : files) {
                @SuppressWarnings("unchecked")
                Map<String, Object> uploadResult = cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap("folder", "auction_images")
                );

                String secureUrl = Objects.toString(uploadResult.get("secure_url"), null);

                Map<String, Object> normalized = new HashMap<>();
                normalized.put("image_url", secureUrl);
                normalized.put("imageUrl", secureUrl);
                normalized.put("secure_url", secureUrl);
                normalized.put("secureUrl", secureUrl);
                normalized.put("public_id", uploadResult.get("public_id"));
                uploadedImages.add(normalized);
            }

            return ResponseEntity.ok(uploadedImages);

        } catch (Exception e) {  // Bắt tất cả các loại lỗi
            e.printStackTrace(); // In đầy đủ stack trace ra console
            return ResponseEntity.internalServerError()
                    .body("Upload thất bại: " + e.getMessage());
        }
    }
}
