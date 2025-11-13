package vn.team9.auction_system.common.dto.image;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ImageRequest {
    private Long productId;
    private String imageUrl;
    
    @JsonProperty("isThumbnail")
    @JsonAlias({"is_thumbnail", "thumbnail"})
    private boolean isThumbnail;

    @JsonProperty("secure_url")
    @JsonAlias({"secureUrl"})
    private String secureUrl;
}
