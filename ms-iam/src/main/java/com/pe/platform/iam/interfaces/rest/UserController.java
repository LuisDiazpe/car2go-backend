package com.pe.platform.iam.interfaces.rest;

import com.pe.platform.iam.domain.model.valueobjects.Roles;
import com.pe.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.pe.platform.iam.interfaces.rest.resources.UserSummaryResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Sprint 3 (Feature D) — Listado de usuarios por rol.
 * Se usa para el ranking de "más recomendados" (vendedores y mecánicos):
 * el frontend pide los usuarios de un rol y luego cruza con sus promedios de reseñas.
 *
 * Endpoint público de lectura (solo datos no sensibles: id, username, rol).
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Consulta de usuarios por rol (para ranking de confianza)")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Lista usuarios de un rol dado. El rol se pasa sin el prefijo ROLE_
     * (ej. /by-role/SELLER, /by-role/MECHANIC).
     */
    @GetMapping("/by-role/{role}")
    @Operation(summary = "Listar usuarios por rol (SELLER, MECHANIC, BUYER)")
    public ResponseEntity<List<UserSummaryResource>> getUsersByRole(@PathVariable String role) {
        Roles roleEnum;
        try {
            // Acepta "SELLER" y lo convierte a ROLE_SELLER
            roleEnum = Roles.valueOf("ROLE_" + role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        List<UserSummaryResource> users = userRepository.findByRoleName(roleEnum).stream()
                .map(u -> new UserSummaryResource(u.getId(), u.getUsername(), u.getPrimaryRoleName()))
                .toList();

        return ResponseEntity.ok(users);
    }
}
