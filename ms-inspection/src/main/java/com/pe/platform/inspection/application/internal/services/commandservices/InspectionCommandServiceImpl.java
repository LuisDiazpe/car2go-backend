package com.pe.platform.inspection.application.internal.services.commandservices;

import com.pe.platform.inspection.domain.model.aggregates.Inspection;
import com.pe.platform.inspection.domain.model.commands.*;
import com.pe.platform.inspection.domain.services.InspectionCommandService;
import com.pe.platform.inspection.infrastructure.acl.vehicle.VehicleAclService;
import com.pe.platform.inspection.infrastructure.persistence.jpa.InspectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Application service for inspection use cases.
 * US-11: Vendedor solicita certificacion -> crea Inspection(PENDING)
 * US-13: Mecanico se asigna -> estado IN_PROGRESS
 * US-15: Mecanico aprueba/rechaza -> actualiza Inspection y notifica a ms-vehicle
 *
 * MICROSERVICIOS: la comunicacion con Vehicle ya no es por repositorio directo,
 * sino via REST (OpenFeign + Circuit Breaker) a traves de VehicleAclService.
 */
@Service
@Transactional
public class InspectionCommandServiceImpl implements InspectionCommandService {

    private final InspectionRepository inspectionRepository;
    private final VehicleAclService vehicleAclService;

    public InspectionCommandServiceImpl(InspectionRepository inspectionRepository,
                                        VehicleAclService vehicleAclService) {
        this.inspectionRepository = inspectionRepository;
        this.vehicleAclService = vehicleAclService;
    }

    @Override
    public Optional<Inspection> handle(CreateInspectionCommand command) {
        var inspection = new Inspection(command);
        return Optional.of(inspectionRepository.save(inspection));
    }

    @Override
    public Optional<Inspection> handle(AssignMechanicCommand command) {
        return inspectionRepository.findById(command.inspectionId())
                .map(inspection -> {
                    inspection.assignMechanic(command.mechanicProfileId());
                    return inspectionRepository.save(inspection);
                });
    }

    @Override
    public Optional<Inspection> approve(CompleteInspectionCommand command) {
        return inspectionRepository.findById(command.inspectionId())
                .map(inspection -> {
                    if (!inspection.isAssignedTo(command.mechanicProfileId())) {
                        throw new SecurityException("Mechanic not assigned to this inspection");
                    }
                    inspection.approve(command);
                    inspectionRepository.save(inspection);

                    // Notifica a ms-vehicle via REST (Feign + Circuit Breaker)
                    vehicleAclService.markReviewed(inspection.getVehicleId());

                    return inspection;
                });
    }

    @Override
    public Optional<Inspection> reject(Long inspectionId, Long mechanicProfileId, String notes) {
        return inspectionRepository.findById(inspectionId)
                .map(inspection -> {
                    if (!inspection.isAssignedTo(mechanicProfileId)) {
                        throw new SecurityException("Mechanic not assigned to this inspection");
                    }
                    inspection.reject(notes);
                    inspectionRepository.save(inspection);

                    // Notifica a ms-vehicle via REST (Feign + Circuit Breaker)
                    vehicleAclService.markRejected(inspection.getVehicleId());

                    return inspection;
                });
    }
}
