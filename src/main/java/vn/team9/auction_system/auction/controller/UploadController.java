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
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", "auction_images")
            );

            Map<String, Object> response = new HashMap<>();
            response.put("image_url", uploadResult.get("secure_url"));
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
                Map<String, Object> uploadResult = cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap("folder", "auction_images")
                );
                uploadedImages.add(uploadResult);
            }

            return ResponseEntity.ok(uploadedImages);

        } catch (Exception e) {  // Bắt tất cả các loại lỗi
            e.printStackTrace(); // In đầy đủ stack trace ra console
            return ResponseEntity.internalServerError()
                    .body("Upload thất bại: " + e.getMessage());
        }
    }
}
