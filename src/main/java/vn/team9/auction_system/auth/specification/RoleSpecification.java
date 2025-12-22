package vn.team9.auction_system.auth.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import vn.team9.auction_system.user.model.Role;

public final class RoleSpecification {

    private RoleSpecification() {
    }

    public static Specification<Role> filter(String name, Boolean isActive) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            predicates.getExpressions().add(cb.isFalse(root.get("isDeleted")));

            if (StringUtils.hasText(name)) {
                predicates.getExpressions().add(cb.like(cb.lower(root.get("roleName")), "%" + name.toLowerCase() + "%"));
            }
            if (isActive != null) {
                predicates.getExpressions().add(cb.equal(root.get("isActive"), isActive));
            }
            return predicates;
        };
    }
}
