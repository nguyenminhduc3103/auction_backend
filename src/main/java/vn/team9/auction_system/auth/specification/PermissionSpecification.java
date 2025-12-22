package vn.team9.auction_system.auth.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import vn.team9.auction_system.auth.model.Permission;

public final class PermissionSpecification {

    private PermissionSpecification() {
    }

    public static Specification<Permission> filter(String name, String module, String method) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            predicates.getExpressions().add(cb.isFalse(root.get("isDeleted")));

            if (StringUtils.hasText(name)) {
                predicates.getExpressions().add(cb.like(cb.lower(root.get("permissionName")), "%" + name.toLowerCase() + "%"));
            }
            if (StringUtils.hasText(module)) {
                predicates.getExpressions().add(cb.like(cb.lower(root.get("module")), "%" + module.toLowerCase() + "%"));
            }
            if (StringUtils.hasText(method)) {
                predicates.getExpressions().add(cb.equal(cb.lower(root.get("method")), method.toLowerCase()));
            }
            return predicates;
        };
    }
}
