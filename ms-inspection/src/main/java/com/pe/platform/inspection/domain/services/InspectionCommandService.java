package com.pe.platform.inspection.domain.services;

import com.pe.platform.inspection.domain.model.aggregates.Inspection;
import com.pe.platform.inspection.domain.model.commands.*;

import java.util.Optional;

public interface InspectionCommandService {
    Optional<Inspection> handle(CreateInspectionCommand command);
    Optional<Inspection> handle(AssignMechanicCommand command);
    Optional<Inspection> approve(CompleteInspectionCommand command);
    Optional<Inspection> reject(Long inspectionId, Long mechanicProfileId, String notes);
}
