package com.pe.platform.vehicle.interfaces.rest.transform;

import com.pe.platform.vehicle.domain.model.aggregates.Vehicle;
import com.pe.platform.vehicle.interfaces.rest.resources.VehicleResource;

public class VehicleResourceFromEntityAssembler {

    public static VehicleResource toResourceFromEntity(Vehicle vehicle) {
        return new VehicleResource(
                vehicle.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getPrice(),
                vehicle.getColor(),
                vehicle.getTransmission(),
                vehicle.getEngine(),
                vehicle.getMileage(),
                vehicle.getDoors(),
                vehicle.getPlate(),
                vehicle.getLocation(),
                vehicle.getDescription(),
                vehicle.getImages(),
                vehicle.getFuel(),
                vehicle.getTopSpeed(),
                vehicle.getContactName(),
                vehicle.getContactPhone(),
                vehicle.getContactEmail(),
                vehicle.getSellerProfileId(),
                vehicle.getStatus().name(),
                vehicle.getCreatedAt() != null ? vehicle.getCreatedAt().toString() : null
        );
    }
}
