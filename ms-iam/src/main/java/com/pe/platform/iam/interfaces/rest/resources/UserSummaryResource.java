package com.pe.platform.iam.interfaces.rest.resources;

/**
 * Datos públicos de un usuario para listados (ranking de confianza).
 * NO expone email ni datos sensibles, solo lo necesario para mostrar.
 */
public record UserSummaryResource(Long id, String username, String role) {}
