package com.pe.platform.userinteraction.interfaces.rest.resources;

/** Datos para crear un comentario. */
public record CreateCommentResource(Long targetProfileId, String content) {}
