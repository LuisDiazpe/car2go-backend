package com.pe.platform.inspection.interfaces.rest.resources;

public record InspectionResource(
        Long id,
        Long vehicleId,
        Long mechanicProfileId,
        Long sellerProfileId,
        String status,
        String mechanicNotes,
        String certificateDetails,
        String scheduledAt,
        String completedAt,
        String createdAt
) {}
