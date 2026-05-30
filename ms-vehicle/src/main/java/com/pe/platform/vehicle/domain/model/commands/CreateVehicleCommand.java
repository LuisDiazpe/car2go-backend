package com.pe.platform.vehicle.domain.model.commands;

import java.util.List;

public record CreateVehicleCommand(
        String brand,
        String model,
        String year,
        double price,
        String color,
        String transmission,
        String engine,
        double mileage,
        String doors,
        String plate,
        String location,
        String description,
        List<String> images,
        String fuel,
        int topSpeed,
        String contactName,
        String contactPhone,
        String contactEmail,
        Long sellerProfileId
) {}
