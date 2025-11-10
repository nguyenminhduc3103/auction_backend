package vn.team9.auction_system.product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.team9.auction_system.product.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

	@EntityGraph(attributePaths = {"images", "seller"})
	List<Product> findAllByIsDeletedFalse();

	@EntityGraph(attributePaths = {"images", "seller"})
	Page<Product> findAllByIsDeletedFalse(Pageable pageable);

	@EntityGraph(attributePaths = {"images", "seller"})
	Optional<Product> findByProductIdAndIsDeletedFalse(Long productId);

	@EntityGraph(attributePaths = {"images", "seller"})
	List<Product> findBySeller_UserIdAndIsDeletedFalse(Long sellerId);
}
