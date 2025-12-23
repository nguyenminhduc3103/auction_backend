package vn.team9.auction_system.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.team9.auction_system.transaction.model.TransactionAfterAuction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionAfterAuctionRepository extends JpaRepository<TransactionAfterAuction, Long> {
    Optional<TransactionAfterAuction> findByAuction_AuctionId(Long auctionId);

    // Lấy tất cả giao dịch có buyer hoặc seller cụ thể
    List<TransactionAfterAuction> findByBuyer_UserIdOrSeller_UserId(Long buyerId, Long sellerId);

    List<TransactionAfterAuction> findByStatusAndUpdatedAtBefore(String status, LocalDateTime time);

    // Lấy ra các sản phẩm đã thắng của người dùng theo status
    @Query("""
        SELECT t
        FROM TransactionAfterAuction t
        JOIN FETCH t.auction a
        JOIN FETCH a.product p
        WHERE t.buyer.userId = :buyerId
          AND (:status IS NULL OR t.status = :status)
        ORDER BY t.updatedAt DESC
    """)
    List<TransactionAfterAuction> findWonAuctions(
            @Param("buyerId") Long buyerId,
            @Param("status") String status
    );

    Optional<TransactionAfterAuction> findByTransactionIdAndBuyerUserId(
            Long transactionId,
            Long buyerUserId
    );
}
