package vn.team9.auction_system.auction.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final Cloudinary cloudinary;

    @PostMapping
    @PreAuthorize("hasAuthority('POST:/api/upload')")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(uploadSingleFile(file));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Upload failed: " + e.getMessage());
        }
    }

    @PostMapping("/multiple")
    @PreAuthorize("hasAuthority('POST:/api/upload/multiple')")
    public ResponseEntity<?> uploadMultipleImages(@RequestParam("files") MultipartFile[] files) {
        try {
            List<Map<String, Object>> uploadedImages = new ArrayList<>();

            for (MultipartFile file : files) {
                uploadedImages.add(uploadSingleFile(file));
            }

            return ResponseEntity.ok(uploadedImages);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Upload failed: " + e.getMessage());
        }
    }

    private Map<String, Object> uploadSingleFile(MultipartFile file) throws Exception {
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

        return response;
    }
}
