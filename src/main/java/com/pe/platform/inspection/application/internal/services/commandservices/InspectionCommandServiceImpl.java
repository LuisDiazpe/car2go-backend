package com.pe.platform.inspection.application.internal.services.commandservices;

import com.pe.platform.inspection.domain.model.aggregates.Inspection;
import com.pe.platform.inspection.domain.model.commands.*;
import com.pe.platform.inspection.domain.services.InspectionCommandService;
import com.pe.platform.inspection.infrastructure.persistence.jpa.InspectionRepository;
import com.pe.platform.vehicle.domain.model.queries.GetVehicleByIdQuery;
import com.pe.platform.vehicle.domain.services.VehicleQueryService;
import com.pe.platform.vehicle.infrastructure.persistence.jpa.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Application service for inspection use cases.
 * US-11: Vendedor solicita certificación → crea Inspection(PENDING)
 * US-13: Mecánico se asigna → estado IN_PROGRESS
 * US-15: Mecánico aprueba/rechaza → actualiza Inspection + Vehicle status
 */
@Service
@Transactional
public class InspectionCommandServiceImpl implements InspectionCommandService {

    private final InspectionRepository inspectionRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleQueryService vehicleQueryService;

    public InspectionCommandServiceImpl(InspectionRepository inspectionRepository,
                                        VehicleRepository vehicleRepository,
                                        VehicleQueryService vehicleQueryService) {
        this.inspectionRepository = inspectionRepository;
        this.vehicleRepository = vehicleRepository;
        this.vehicleQueryService = vehicleQueryService;
    }

    @Override
    public Optional<Inspection> handle(CreateInspectionCommand command) {
        // Verificar que el vehículo existe
        vehicleQueryService.handle(new GetVehicleByIdQuery(command.vehicleId()))
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + command.vehicleId()));

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

                    // Actualizar estado del vehículo a REVIEWED (anti-corruption layer)
                    vehicleRepository.findById(inspection.getVehicleId())
                            .ifPresent(vehicle -> {
                                vehicle.markAsReviewed();
                                vehicleRepository.save(vehicle);
                            });

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

                    // Actualizar estado del vehículo a REJECTED
                    vehicleRepository.findById(inspection.getVehicleId())
                            .ifPresent(vehicle -> {
                                vehicle.markAsRejected();
                                vehicleRepository.save(vehicle);
                            });

                    return inspection;
                });
    }
}
