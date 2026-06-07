package com.pe.platform.inspection.application.internal.services.queryservices;

import com.pe.platform.inspection.domain.model.aggregates.Inspection;
import com.pe.platform.inspection.domain.model.queries.*;
import com.pe.platform.inspection.domain.model.valueobjects.InspectionStatus;
import com.pe.platform.inspection.domain.services.InspectionQueryService;
import com.pe.platform.inspection.infrastructure.persistence.jpa.InspectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class InspectionQueryServiceImpl implements InspectionQueryService {

    private final InspectionRepository inspectionRepository;

    public InspectionQueryServiceImpl(InspectionRepository inspectionRepository) {
        this.inspectionRepository = inspectionRepository;
    }

    @Override
    public Optional<Inspection> handle(GetInspectionByIdQuery query) {
        return inspectionRepository.findById(query.inspectionId());
    }

    @Override
    public List<Inspection> handle(GetInspectionsByVehicleIdQuery query) {
        return inspectionRepository.findByVehicleId(query.vehicleId());
    }

    @Override
    public List<Inspection> handle(GetPendingInspectionsQuery query) {
        return inspectionRepository.findByStatus(InspectionStatus.PENDING);
    }

    @Override
    public List<Inspection> handle(GetAssignedInspectionsQuery query) {
        // Inspecciones que el mecánico tiene en progreso (asignadas a él)
        return inspectionRepository.findByMechanicProfileIdAndStatus(
                query.mechanicProfileId(), InspectionStatus.IN_PROGRESS);
    }
}
