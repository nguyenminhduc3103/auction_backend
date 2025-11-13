package vn.team9.auction_system.transaction.model;

import jakarta.persistence.*;
import lombok.Data;
import vn.team9.auction_system.auction.model.Auction;
import vn.team9.auction_system.user.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transactionafterauction")
public class TransactionAfterAuction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "txn_id")
    private Long transactionId;

    @ManyToOne
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @ManyToOne
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 20)
    private String status; // PENDING, PAID, SHIPPED, DONE, CANCELLED

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
