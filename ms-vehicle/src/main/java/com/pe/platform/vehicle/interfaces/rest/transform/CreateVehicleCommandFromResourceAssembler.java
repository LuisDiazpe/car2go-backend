package com.pe.platform.vehicle.interfaces.rest.transform;

import com.pe.platform.vehicle.domain.model.commands.CreateVehicleCommand;
import com.pe.platform.vehicle.interfaces.rest.resources.CreateVehicleResource;

public class CreateVehicleCommandFromResourceAssembler {

    public static CreateVehicleCommand toCommandFromResource(CreateVehicleResource resource, Long sellerProfileId) {
        return new CreateVehicleCommand(
                resource.brand(), resource.model(), resource.year(), resource.price(),
                resource.color(), resource.transmission(), resource.engine(), resource.mileage(),
                resource.doors(), resource.plate(), resource.location(), resource.description(),
                resource.images(), resource.fuel(), resource.topSpeed(),
                resource.contactName(), resource.contactPhone(), resource.contactEmail(),
                sellerProfileId
        );
    }
}
