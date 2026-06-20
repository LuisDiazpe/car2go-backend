package com.pe.platform.userinteraction.interfaces.rest.resources;

/** Datos para crear/actualizar una reseña (estrellas 1-5 + comentario opcional). */
public record CreateReviewResource(Long targetProfileId, Integer rating, String comment) {}
