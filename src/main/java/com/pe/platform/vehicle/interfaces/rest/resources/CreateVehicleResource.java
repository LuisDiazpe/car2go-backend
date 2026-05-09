package com.pe.platform.vehicle.interfaces.rest.resources;

import jakarta.validation.constraints.*;
import java.util.List;

public record CreateVehicleResource(
        @NotBlank String brand,
        @NotBlank String model,
        @NotBlank String year,
        @Positive double price,
        @NotBlank String color,
        @NotBlank String transmission,
        @NotBlank String engine,
        @PositiveOrZero double mileage,
        @NotBlank String doors,
        @NotBlank String plate,
        @NotBlank String location,
        @NotBlank String description,
        List<String> images,
        @NotBlank String fuel,
        @PositiveOrZero int topSpeed,
        @NotBlank String contactName,
        @NotBlank String contactPhone,
        @NotBlank @Email String contactEmail
) {}
