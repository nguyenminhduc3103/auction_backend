package vn.team9.auction_system.common.dto.image;

import lombok.Data;

@Data
public class ImageRequest {
    private Long productId;
    private String imageUrl;
    private boolean isThumbnail;
}
