package com.pe.platform.inspection.domain.model.valueobjects;

/**
 * Estados de la inspección técnica
 * US-11: Vendedor solicita certificación → PENDING
 * US-13: Mecánico asignado → IN_PROGRESS
 * US-14/15: Mecánico completa → APPROVED o REJECTED
 */
public enum InspectionStatus {
    PENDING,
    IN_PROGRESS,
    APPROVED,
    REJECTED
}
