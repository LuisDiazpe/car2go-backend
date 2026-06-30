package com.pe.platform.inspection.interfaces.rest.transform;

import com.pe.platform.inspection.domain.model.aggregates.Inspection;
import com.pe.platform.inspection.interfaces.rest.resources.InspectionResource;

public class InspectionResourceFromEntityAssembler {

    public static InspectionResource toResourceFromEntity(Inspection inspection) {
        return new InspectionResource(
                inspection.getId(),
                inspection.getVehicleId(),
                inspection.getMechanicProfileId(),
                inspection.getSellerProfileId(),
                inspection.getStatus().name(),
                inspection.getMechanicNotes(),
                inspection.getCertificateDetails(),
                inspection.getInspectionFee(),
                inspection.getScheduledAt() != null ? inspection.getScheduledAt().toString() : null,
                inspection.getCompletedAt() != null ? inspection.getCompletedAt().toString() : null,
                inspection.getCreatedAt() != null ? inspection.getCreatedAt().toString() : null
        );
    }
}
