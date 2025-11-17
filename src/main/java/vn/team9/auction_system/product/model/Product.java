package vn.team9.auction_system.product.model;

import jakarta.persistence.*;
import lombok.Data;
import vn.team9.auction_system.user.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "Product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "categories", length = 100)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_price", precision = 18, scale = 2)
    private BigDecimal startPrice;

    @Column(name = "estimate_price", precision = 18, scale = 2)
    private BigDecimal estimatePrice;

    @Column(name = "deposit", precision = 18, scale = 2)
    private BigDecimal deposit;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(length = 20)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();
}
