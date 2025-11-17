package vn.team9.auction_system.auction.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.team9.auction_system.auction.model.Auction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuctionRepository extends JpaRepository<Auction, Long>, JpaSpecificationExecutor<Auction> {
    List<Auction> findByStatus(String status);
    List<Auction> findByStatusAndEndTimeBefore(String status, LocalDateTime time);

    @Override
    @EntityGraph(attributePaths = {
            "product",
            "product.images",
            "product.seller"
    })
    Page<Auction> findAll(Specification<Auction> spec, Pageable pageable);

    // Lấy thông tin chi tiết của 1 auction theo id
    @Query("""
    SELECT a FROM Auction a
    JOIN FETCH a.product p
    JOIN FETCH p.seller s
    LEFT JOIN FETCH p.images imgs
    WHERE a.auctionId = :id
""")
    Optional<Auction> findByIdWithSellerAndImages(Long id);

    @Query(value = "SELECT end_time FROM auction WHERE auction_id = :id", nativeQuery = true)
    String checkEnd(@Param("id") Long id);

}
