package vn.team9.auction_system.user.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import vn.team9.auction_system.auth.model.Permission;
import vn.team9.auction_system.common.base.AuditableEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "Role")
// Column names are quoted when globally_quoted_identifiers=true, so quote here
// too
@SQLRestriction("\"is_deleted\" = false")
public class Role extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "role_name", nullable = false, unique = true, length = 200)
    private String roleName;

    @Column(length = 500)
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(
            // Match DB join table name (PascalCase)
            name = "RolePermission",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> permissions = new HashSet<>();

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
    private List<User> users;
}
