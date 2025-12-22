package vn.team9.auction_system.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.team9.auction_system.auth.model.Permission;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {
    boolean existsByApiPathIgnoreCaseAndMethodIgnoreCaseAndIsDeletedFalse(String apiPath, String method);
    boolean existsByPermissionNameIgnoreCaseAndIsDeletedFalse(String permissionName);
    Optional<Permission> findByPermissionIdAndIsDeletedFalse(Long permissionId);
}
