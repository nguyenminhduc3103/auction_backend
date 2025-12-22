package vn.team9.auction_system.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.team9.auction_system.user.model.Role;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {
    boolean existsByRoleNameIgnoreCaseAndIsDeletedFalse(String roleName);
    Optional<Role> findByRoleIdAndIsDeletedFalse(Long roleId);
    Optional<Role> findByRoleNameIgnoreCaseAndIsDeletedFalse(String roleName);
}
