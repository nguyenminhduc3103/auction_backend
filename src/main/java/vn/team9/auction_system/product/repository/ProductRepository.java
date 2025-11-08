package vn.team9.auction_system.product.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import vn.team9.auction_system.product.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

	@Override
	@NonNull
	@EntityGraph(attributePaths = {"images", "seller"})
	List<Product> findAll();

	@EntityGraph(attributePaths = {"images", "seller"})
	List<Product> findBySeller_UserId(Long sellerId);

	@EntityGraph(attributePaths = {"images", "seller"})
	Optional<Product> findByProductId(Long productId);
}
