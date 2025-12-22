package vn.team9.auction_system.common.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CreateResponse {
    private Long id;
    private LocalDateTime createdAt;
}
