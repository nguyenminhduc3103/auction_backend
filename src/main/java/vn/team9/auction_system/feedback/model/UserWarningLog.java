package vn.team9.auction_system.feedback.model;

import jakarta.persistence.*;
import lombok.Data;
import vn.team9.auction_system.transaction.model.TransactionAfterAuction;
import vn.team9.auction_system.user.model.User;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "UserWarningLog")
public class UserWarningLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    // Người dùng bị cảnh báo
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Giao dịch liên quan
    @ManyToOne
    @JoinColumn(name = "transaction_id", nullable = false)
    private TransactionAfterAuction transaction;

    // Loại cảnh báo: SHIPPED_NOT_PAID
    @Column(name = "type", length = 50, nullable = false)
    private String type;

    // Trạng thái cảnh báo: VIOLATION
    @Column(name = "status", length = 50, nullable = false)
    private String status;

    // Mô tả, default "FOUND NG BEHAVIOR"
    @Column(name = "description", nullable = false)
    private String description = "FOUND NG BEHAVIOR";

    // Số lần vi phạm trước đó (ví dụ lần 1, lần 2...)
    @Column(name = "violation_count", nullable = false)
    private Integer violationCount = 1;

    // Thời gian tạo log
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (type == null) type = "SHIPPED_NOT_PAID";
        if (status == null) status = "VIOLATION";
        if (violationCount == null) violationCount = 1;
    }
}
