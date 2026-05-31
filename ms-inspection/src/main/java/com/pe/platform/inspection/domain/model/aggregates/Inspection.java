package com.pe.platform.inspection.domain.model.aggregates;

import com.pe.platform.inspection.domain.model.commands.CreateInspectionCommand;
import com.pe.platform.inspection.domain.model.commands.CompleteInspectionCommand;
import com.pe.platform.inspection.domain.model.valueobjects.InspectionStatus;
import com.pe.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Inspection aggregate root — Vehicle Management BC (inspections sub-domain)
 *
 * US-11: Vendedor solicita certificación
 * US-12: Comprador ve el informe de certificación
 * US-13: Solicitar inspección
 * US-14: Confirmación de inspección programada
 * US-15: Actualización del estado de inspección
 */
@Getter
@Entity
public class Inspection extends AuditableAbstractAggregateRoot<Inspection> {

    /** FK al vehículo a inspeccionar */
    @Column(nullable = false)
    private Long vehicleId;

    /** FK al perfil del mecánico asignado */
    @Column
    private Long mechanicProfileId;

    /** FK al perfil del vendedor que solicitó */
    @Column(nullable = false)
    private Long sellerProfileId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InspectionStatus status;

    @Column(columnDefinition = "TEXT")
    private String mechanicNotes;

    /** Resultado del certificado tras la inspección */
    @Column(columnDefinition = "TEXT")
    private String certificateDetails;

    @Column
    private LocalDateTime scheduledAt;

    @Column
    private LocalDateTime completedAt;

    protected Inspection() {}

    public Inspection(CreateInspectionCommand command) {
        this.vehicleId = command.vehicleId();
        this.sellerProfileId = command.sellerProfileId();
        this.status = InspectionStatus.PENDING;
        this.scheduledAt = command.scheduledAt();
    }

    /** US-13/14: Mecánico acepta y se asigna */
    public void assignMechanic(Long mechanicProfileId) {
        if (this.status != InspectionStatus.PENDING) {
            throw new IllegalStateException("Cannot assign mechanic to an inspection not in PENDING status");
        }
        this.mechanicProfileId = mechanicProfileId;
        this.status = InspectionStatus.IN_PROGRESS;
    }

    /** US-15: Mecánico aprueba con certificado */
    public void approve(CompleteInspectionCommand command) {
        if (this.status != InspectionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot approve inspection not in IN_PROGRESS status");
        }
        this.mechanicNotes = command.notes();
        this.certificateDetails = command.certificateDetails();
        this.status = InspectionStatus.APPROVED;
        this.completedAt = LocalDateTime.now();
    }

    /** US-15: Mecánico rechaza */
    public void reject(String notes) {
        if (this.status != InspectionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot reject inspection not in IN_PROGRESS status");
        }
        this.mechanicNotes = notes;
        this.status = InspectionStatus.REJECTED;
        this.completedAt = LocalDateTime.now();
    }

    public boolean isAssignedTo(Long mechanicProfileId) {
        return mechanicProfileId.equals(this.mechanicProfileId);
    }
}
