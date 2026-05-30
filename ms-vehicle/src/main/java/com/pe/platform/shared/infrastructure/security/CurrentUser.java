package com.pe.platform.shared.infrastructure.security;

import lombok.Getter;

/**
 * Representa al usuario autenticado en un microservicio downstream.
 *
 * No depende de IAM. La identidad llega via headers (X-User-Id, X-User-Role)
 * que el API Gateway inyecta tras validar el JWT. Sustituye a UserDetailsImpl
 * del monolito.
 */
@Getter
public class CurrentUser {
    private final Long id;
    private final String username;
    private final String role;

    public CurrentUser(Long id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }
}
