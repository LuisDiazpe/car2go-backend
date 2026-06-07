package com.pe.platform.inspection.domain.services;

import com.pe.platform.inspection.domain.model.aggregates.Inspection;
import com.pe.platform.inspection.domain.model.queries.*;

import java.util.List;
import java.util.Optional;

public interface InspectionQueryService {
    Optional<Inspection> handle(GetInspectionByIdQuery query);
    List<Inspection> handle(GetInspectionsByVehicleIdQuery query);
    List<Inspection> handle(GetPendingInspectionsQuery query);
    List<Inspection> handle(GetAssignedInspectionsQuery query);
}
