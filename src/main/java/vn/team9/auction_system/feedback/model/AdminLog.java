package vn.team9.auction_system.feedback.model;

import jakarta.persistence.*;
import lombok.Data;
import vn.team9.auction_system.user.model.User;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "adminlog")
public class AdminLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private User admin;

    @Column(columnDefinition = "TEXT")
    private String action;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    private LocalDateTime createdAt = LocalDateTime.now();
}
