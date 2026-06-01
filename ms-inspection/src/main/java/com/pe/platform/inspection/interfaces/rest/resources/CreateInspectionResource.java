package com.pe.platform.inspection.interfaces.rest.resources;

import java.time.LocalDateTime;

public record CreateInspectionResource(Long vehicleId, LocalDateTime scheduledAt) {}
