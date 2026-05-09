package com.pe.platform.vehicle.interfaces.rest.transform;

import com.pe.platform.vehicle.domain.model.commands.UpdateVehicleCommand;
import com.pe.platform.vehicle.interfaces.rest.resources.UpdateVehicleResource;

public class UpdateVehicleCommandFromResourceAssembler {

    public static UpdateVehicleCommand toCommandFromResource(Long vehicleId, UpdateVehicleResource resource) {
        return new UpdateVehicleCommand(
                vehicleId,
                resource.brand(), resource.model(), resource.year(), resource.price(),
                resource.color(), resource.transmission(), resource.engine(), resource.mileage(),
                resource.doors(), resource.location(), resource.description(),
                resource.images(), resource.fuel(), resource.topSpeed(),
                resource.contactName(), resource.contactPhone(), resource.contactEmail()
        );
    }
}
