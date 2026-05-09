package com.pe.platform.vehicle.interfaces.rest.resources;

import java.util.List;

public record UpdateVehicleResource(
        String brand,
        String model,
        String year,
        double price,
        String color,
        String transmission,
        String engine,
        double mileage,
        String doors,
        String location,
        String description,
        List<String> images,
        String fuel,
        int topSpeed,
        String contactName,
        String contactPhone,
        String contactEmail
) {}
