package com.pe.platform.iam.interfaces.rest.resources;

/**
 * US-02: El usuario inicia sesión con su email O su username (cualquiera de los dos).
 * El campo 'identifier' es el preferido; 'username' se mantiene por compatibilidad.
 */
public record SignInResource(String identifier, String username, String password) {

    /** Devuelve el identificador a usar: prioriza 'identifier', si no, usa 'username'. */
    public String resolveIdentifier() {
        if (identifier != null && !identifier.isBlank()) return identifier;
        return username;
    }
}
