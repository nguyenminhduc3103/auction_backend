package vn.team9.auction_system.feedback.model;

import jakarta.persistence.*;
import lombok.Data;
import vn.team9.auction_system.user.model.User;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "noti_id")
    private Long notiId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String type;

    @Column(name = "is_read")
    private Boolean isRead = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}
