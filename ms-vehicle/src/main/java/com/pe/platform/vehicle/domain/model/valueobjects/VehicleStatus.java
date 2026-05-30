package com.pe.platform.vehicle.domain.model.valueobjects;

/**
 * Estado del vehículo en el flujo de inspección.
 * PENDING  → recién publicado por el vendedor (US-03)
 * REVIEWED → inspeccionado y certificado por mecánico (US-11/12/13)
 * REJECTED → inspeccionado y rechazado por mecánico
 * SOLD     → transacción completada (US-16/17)
 *
 * CORRECCIÓN: Clase renombrada de 'vehicleStatus' a 'VehicleStatus'
 * para cumplir la convención Java de nombres de clase en PascalCase.
 */
public enum VehicleStatus {
    PENDING,
    REVIEWED,
    REJECTED,
    SOLD
}
