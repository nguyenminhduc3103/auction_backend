package vn.team9.auction_system.auction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.team9.auction_system.auction.model.Bid;
import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByAuction_AuctionId(Long auctionId);
    List<Bid> findByBidder_UserId(Long userId);
    Optional<Bid> findTopByAuction_AuctionIdOrderByBidAmountDesc(Long auctionId);

    @Query(value = """
    SELECT COUNT(DISTINCT bidder_id)
    FROM Bid
    WHERE auction_id = :auctionId
""", nativeQuery = true)
    Long countDistinctBidders(Long auctionId);
}
