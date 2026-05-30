package com.pe.platform.vehicle.application.internal.services.commandservices;

import com.pe.platform.vehicle.domain.model.aggregates.Vehicle;
import com.pe.platform.vehicle.domain.model.commands.CreateVehicleCommand;
import com.pe.platform.vehicle.domain.model.commands.UpdateVehicleCommand;
import com.pe.platform.vehicle.domain.services.VehicleCommandService;
import com.pe.platform.vehicle.infrastructure.persistence.jpa.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class VehicleCommandServiceImpl implements VehicleCommandService {

    private final VehicleRepository vehicleRepository;

    public VehicleCommandServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public Optional<Vehicle> handle(CreateVehicleCommand command) {
        if (vehicleRepository.existsByPlate(command.plate())) {
            throw new IllegalArgumentException("A vehicle with plate '" + command.plate() + "' already exists");
        }
        var vehicle = new Vehicle(command);
        return Optional.of(vehicleRepository.save(vehicle));
    }

    @Override
    public Optional<Vehicle> handle(UpdateVehicleCommand command) {
        return vehicleRepository.findById(command.vehicleId())
                .map(vehicle -> {
                    vehicle.update(command);
                    return vehicleRepository.save(vehicle);
                });
    }

    @Override
    public void deleteVehicle(Long vehicleId, Long sellerProfileId) {
        var vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + vehicleId));
        if (!vehicle.isOwnedBy(sellerProfileId)) {
            throw new SecurityException("Seller " + sellerProfileId + " does not own vehicle " + vehicleId);
        }
        vehicleRepository.delete(vehicle);
    }
}
