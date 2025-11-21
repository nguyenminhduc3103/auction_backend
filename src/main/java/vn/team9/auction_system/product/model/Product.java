package vn.team9.auction_system.product.model;

import jakarta.persistence.*;
import lombok.Data;
import vn.team9.auction_system.user.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "Product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_price", precision = 18, scale = 2)
    private BigDecimal startPrice;

    @Column(name = "estimate_price", precision = 18, scale = 2)
    private BigDecimal estimatePrice;

    @Column(name = "categories")
    private String category;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images;

    @Column(length = 20)
    private String status; // AVAILABLE, AUCTIONED, SOLD

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
