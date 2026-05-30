package com.pe.platform.inspection.infrastructure.acl.vehicle;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * Anti-Corruption Layer (ACL) hacia el microservicio ms-vehicle.
 *
 * En el monolito, Inspection accedia directo al VehicleRepository.
 * Ahora que Vehicle es un microservicio independiente con su propia BD,
 * la comunicacion se hace via REST usando OpenFeign.
 *
 * Eureka resuelve "ms-vehicle" a la direccion real del servicio (lb://).
 */
@FeignClient(name = "ms-vehicle")
public interface VehicleClient {

    /** Cambia el estado del vehiculo a REVIEWED cuando el mecanico aprueba */
    @PutMapping("/api/v1/vehicles/{id}/mark-reviewed")
    void markVehicleReviewed(@PathVariable("id") Long vehicleId);

    /** Cambia el estado del vehiculo a REJECTED cuando el mecanico rechaza */
    @PutMapping("/api/v1/vehicles/{id}/mark-rejected")
    void markVehicleRejected(@PathVariable("id") Long vehicleId);
}
