package vn.team9.auction_system.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.team9.auction_system.transaction.model.TransactionAfterAuction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionAfterAuctionRepository extends JpaRepository<TransactionAfterAuction, Long> {
    Optional<TransactionAfterAuction> findByAuction_AuctionId(Long auctionId);

    // Lấy tất cả giao dịch có buyer hoặc seller cụ thể
    List<TransactionAfterAuction> findByBuyer_UserIdOrSeller_UserId(Long buyerId, Long sellerId);

    List<TransactionAfterAuction> findByStatusAndUpdatedAtBefore(String status, LocalDateTime time);
}
