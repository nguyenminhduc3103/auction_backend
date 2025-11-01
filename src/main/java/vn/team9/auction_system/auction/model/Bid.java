package vn.team9.auction_system.auction.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.team9.auction_system.user.model.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "\"Bid\"")
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bid_id")
    private Long bidId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    private Auction auction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bidder_id")
    private User bidder;

    @Column(name = "bid_amount")
    private BigDecimal bidAmount;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "max_autobid_amount")
    private BigDecimal maxAutobidAmount;

    @Column(name = "step_autobid_amount")
    private BigDecimal stepAutoBidAmount;

    @Column(name = "is_auto")
    private Boolean isAuto = false;

    @Column(name = "is_highest")
    private Boolean isHighest = false;
}
