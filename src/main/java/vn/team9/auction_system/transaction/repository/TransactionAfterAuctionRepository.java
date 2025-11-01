package vn.team9.auction_system.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.team9.auction_system.transaction.model.TransactionAfterAuction;

public interface TransactionAfterAuctionRepository extends JpaRepository<TransactionAfterAuction, Long> {
}
