package com.pe.platform.iam.domain.model.aggregates;

import com.pe.platform.iam.domain.model.commands.SignUpCommand;
import com.pe.platform.iam.domain.model.entities.Role;
import com.pe.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * User aggregate root for IAM Bounded Context.
 * Handles authentication identity — separate from Profile (User Interaction BC).
 */
@Getter
@Entity
@Table(name = "users")
public class User extends AuditableAbstractAggregateRoot<User> {

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles = new ArrayList<>();

    protected User() {}

    public User(SignUpCommand command, String passwordHash, List<Role> roles) {
        this.username = command.username();
        this.email = command.email();
        this.passwordHash = passwordHash;
        this.roles = roles;
    }

    public boolean hasRole(String roleName) {
        return roles.stream()
                .anyMatch(role -> role.getName().name().equals(roleName));
    }

    public String getPrimaryRoleName() {
        return roles.isEmpty() ? "UNKNOWN" : roles.get(0).getName().name();
    }
}
