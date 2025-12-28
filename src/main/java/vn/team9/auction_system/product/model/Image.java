package vn.team9.auction_system.product.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Image")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "is_thumbnail", nullable = false)
    private Boolean isThumbnail = false;
}

