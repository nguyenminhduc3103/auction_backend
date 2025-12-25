package vn.team9.auction_system.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.team9.auction_system.auth.model.Permission;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {
    boolean existsByApiPathIgnoreCaseAndMethodIgnoreCaseAndIsDeletedFalse(String apiPath, String method);

    boolean existsByPermissionNameIgnoreCaseAndIsDeletedFalse(String permissionName);

    Optional<Permission> findByPermissionIdAndIsDeletedFalse(Long permissionId);

    // Query permissions through rolepermission join table
    @Query(value = """
            SELECT p.* FROM permission p
            INNER JOIN rolepermission rp ON p.permission_id = rp.permission_id
            WHERE rp.role_id = :roleId AND (p.is_deleted = 0 OR p.is_deleted IS NULL)
            """, nativeQuery = true)
    List<Permission> findByRoleId(@Param("roleId") Long roleId);
}
