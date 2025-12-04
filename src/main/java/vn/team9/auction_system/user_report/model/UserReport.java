package vn.team9.auction_system.user_report.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "user_reports")
public class UserReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // id của user bị report

    private String content; // nội dung report

    private LocalDateTime createdAt;

    public UserReport() {
        this.createdAt = LocalDateTime.now();
    }

}
