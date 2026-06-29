package com.pe.platform.payment.infrastructure.acl;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * Anti-Corruption Layer (ACL) hacia ms-vehicle.
 * Cuando se completa una compra, ms-payment le avisa a ms-vehicle (dueño del
 * catálogo) que marque el vehículo como SOLD. Comunicación síncrona vía OpenFeign,
 * con la dirección resuelta por Eureka.
 */
@FeignClient(name = "ms-vehicle")
public interface VehicleClient {

    @PutMapping("/api/v1/vehicles/{id}/mark-sold")
    void markSold(@PathVariable("id") Long vehicleId);
}
