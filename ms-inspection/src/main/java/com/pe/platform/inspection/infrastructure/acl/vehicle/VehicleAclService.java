package com.pe.platform.inspection.infrastructure.acl.vehicle;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Facade resiliente sobre VehicleClient
 *
 * Aplica Circuit Breaker (Resilience4j): si ms-vehicle esta caido,
 * no se propaga la falla en cascada. El metodo fallback registra el
 * problema sin romper el flujo de aprobacion de la inspeccion
 *
 * atributo de calidad de RESILIENCIA / DISPONIBILIDAD
 */
@Service
public class VehicleAclService {

    private static final Logger log = LoggerFactory.getLogger(VehicleAclService.class);
    private final VehicleClient vehicleClient;

    public VehicleAclService(VehicleClient vehicleClient) {
        this.vehicleClient = vehicleClient;
    }

    @CircuitBreaker(name = "vehicleService", fallbackMethod = "markReviewedFallback")
    public void markReviewed(Long vehicleId) {
        vehicleClient.markVehicleReviewed(vehicleId);
        log.info("Vehiculo {} marcado como REVIEWED via ms-vehicle", vehicleId);
    }

    @CircuitBreaker(name = "vehicleService", fallbackMethod = "markRejectedFallback")
    public void markRejected(Long vehicleId) {
        vehicleClient.markVehicleRejected(vehicleId);
        log.info("Vehiculo {} marcado como REJECTED via ms-vehicle", vehicleId);
    }

    // Fallbacks: se ejecutan si ms-vehicle no responde
    private void markReviewedFallback(Long vehicleId, Throwable t) {
        log.error("ms-vehicle no disponible. No se pudo marcar REVIEWED el vehiculo {}. Causa: {}",
                vehicleId, t.getMessage());
        // En produccion: encolar el evento para reintento posterior
    }

    private void markRejectedFallback(Long vehicleId, Throwable t) {
        log.error("ms-vehicle no disponible. No se pudo marcar REJECTED el vehiculo {}. Causa: {}",
                vehicleId, t.getMessage());
    }
}
