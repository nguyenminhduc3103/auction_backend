package vn.team9.auction_system.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.team9.auction_system.product.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
