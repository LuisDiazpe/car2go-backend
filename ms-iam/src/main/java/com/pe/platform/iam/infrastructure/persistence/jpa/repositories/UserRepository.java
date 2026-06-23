package com.pe.platform.iam.infrastructure.persistence.jpa.repositories;

import com.pe.platform.iam.domain.model.aggregates.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);

    /**
     * Busca usuarios que tengan un rol específico (ej. ROLE_SELLER, ROLE_MECHANIC).
     * Recorre la relación ManyToMany users -> roles y filtra por el nombre del rol.
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") com.pe.platform.iam.domain.model.valueobjects.Roles roleName);
}
