package com.pe.platform.inspection.domain.model.queries;

/** Inspecciones asignadas a un mecánico (estado IN_PROGRESS). */
public record GetAssignedInspectionsQuery(Long mechanicProfileId) {}
