package vn.team9.auction_system.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import vn.team9.auction_system.common.base.AuditableEntity;
import vn.team9.auction_system.user.model.Role;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(
        name = "Permission",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_permission_api_method", columnNames = {"api_path", "method"}),
                @UniqueConstraint(name = "uk_permission_name", columnNames = {"permission_name"})
        }
)
// Column names are quoted when globally_quoted_identifiers=true, so quote here too
@SQLRestriction("\"is_deleted\" = false")
public class Permission extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long permissionId;

    @Column(name = "permission_name", nullable = false, length = 200)
    private String permissionName;

    @Column(name = "api_path", nullable = false, length = 500)
    private String apiPath;

    @Column(name = "method", nullable = false, length = 20)
    private String method;

    @Column(name = "module", nullable = false, length = 100)
    private String module;

    @Column(name = "description", length = 500)
    private String description;

    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<Role> roles = new HashSet<>();
}
