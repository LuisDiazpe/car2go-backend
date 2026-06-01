package com.pe.platform.iam.domain.model.aggregates;

import com.pe.platform.iam.domain.model.commands.SignUpCommand;
import com.pe.platform.iam.domain.model.entities.Role;
import com.pe.platform.iam.domain.model.valueobjects.Roles;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios de la entidad User (IAM BC)
 * Prueba la logica de roles sin base de datos
 */
class UserTest {

    private User crearUsuario(Roles rol) {
        var command = new SignUpCommand("usuario1", "123456", List.of(rol.name()));
        return new User(command, "hashEncriptado", List.of(new Role(rol)));
    }

    @Test
    @DisplayName("Un usuario creado guarda su username y password hash")
    void crearUsuario_datosBasicos() {
        var user = crearUsuario(Roles.ROLE_SELLER);
        assertEquals("usuario1", user.getUsername());
        assertEquals("hashEncriptado", user.getPasswordHash());
    }

    @Test
    @DisplayName("hasRole devuelve true para el rol asignado")
    void hasRole_rolAsignado_true() {
        var user = crearUsuario(Roles.ROLE_MECHANIC);
        assertTrue(user.hasRole("ROLE_MECHANIC"));
    }

    @Test
    @DisplayName("hasRole devuelve false para un rol no asignado")
    void hasRole_rolNoAsignado_false() {
        var user = crearUsuario(Roles.ROLE_BUYER);
        assertFalse(user.hasRole("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("getPrimaryRoleName devuelve el primer rol del usuario")
    void getPrimaryRoleName_devuelvePrimerRol() {
        var user = crearUsuario(Roles.ROLE_SELLER);
        assertEquals("ROLE_SELLER", user.getPrimaryRoleName());
    }

    @Test
    @DisplayName("Un usuario con varios roles guarda todos")
    void usuarioConVariosRoles() {
        var command = new SignUpCommand("multi", "123456",
                List.of("ROLE_BUYER", "ROLE_SELLER"));
        var user = new User(command, "hash",
                List.of(new Role(Roles.ROLE_BUYER), new Role(Roles.ROLE_SELLER)));

        assertTrue(user.hasRole("ROLE_BUYER"));
        assertTrue(user.hasRole("ROLE_SELLER"));
        assertEquals(2, user.getRoles().size());
    }
}
