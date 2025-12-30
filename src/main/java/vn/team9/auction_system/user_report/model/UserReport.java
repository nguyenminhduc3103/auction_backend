package vn.team9.auction_system.user_report.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "UserReports")
public class UserReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // id của user bị report

    private String content; // nội dung report

    private Long auctionId; // nullable - chỉ có khi report từ auction detail page

    private Long sellerId; // nullable - chỉ có khi report từ order/seller

    private LocalDateTime createdAt;

    public UserReport() {
        this.createdAt = LocalDateTime.now();
    }

}
