package vn.team9.auction_system.auction.repository;

import org.springframework.data.jpa.domain.Specification;
import vn.team9.auction_system.auction.model.Auction;

import java.math.BigDecimal;

public class AuctionSpecification {

    public static Specification<Auction> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.trim().isEmpty())
                return cb.conjunction();
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Auction> excludeStatus(String status) {
        return (root, query, cb) -> status == null
                ? cb.conjunction()
                : cb.notEqual(root.get("status"), status);
    }

    public static Specification<Auction> hasCategory(String category) {
        return (root, query, cb) -> {
            if (category == null || category.isBlank()) return cb.conjunction();

            return cb.like(
                    cb.lower(cb.trim(root.join("product").get("category"))),
                    "%" + category.toLowerCase().trim() + "%"
            );
        };
    }

    public static Specification<Auction> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty())
                return cb.conjunction();

            return cb.like(
                    cb.lower(root.join("product").get("name")),
                    "%" + keyword.toLowerCase() + "%"
            );
        };
    }


    public static Specification<Auction> hasPriceRange(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return cb.conjunction();

            if (min != null && max != null)
                return cb.between(root.get("highestCurrentPrice"), min, max);

            if (min != null)
                return cb.greaterThanOrEqualTo(root.get("highestCurrentPrice"), min);

            return cb.lessThanOrEqualTo(root.get("highestCurrentPrice"), max);
        };
    }

}
