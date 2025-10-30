package vn.team9.auction_system.auction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.team9.auction_system.auction.model.Auction;

import java.time.LocalDateTime;
import java.util.List;

public interface AuctionRepository extends JpaRepository<Auction, Long> {
    List<Auction> findByStatus(String status);
    List<Auction> findByStatusAndEndTimeBefore(String status, LocalDateTime time);
}
