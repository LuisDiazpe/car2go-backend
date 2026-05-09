package com.pe.platform.inspection.interfaces.rest;

import com.pe.platform.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.pe.platform.inspection.domain.model.commands.*;
import com.pe.platform.inspection.domain.model.queries.*;
import com.pe.platform.inspection.domain.services.InspectionCommandService;
import com.pe.platform.inspection.domain.services.InspectionQueryService;
import com.pe.platform.inspection.interfaces.rest.resources.*;
import com.pe.platform.inspection.interfaces.rest.transform.InspectionResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Inspection REST controller.
 * US-11: POST /api/v1/inspections          → vendedor solicita inspección
 * US-12: GET  /api/v1/inspections/vehicle/{id} → comprador ve informe
 * US-13: GET  /api/v1/inspections/pending  → mecánico ve solicitudes pendientes
 * US-14: PUT  /api/v1/inspections/{id}/assign → mecánico se asigna
 * US-15: PUT  /api/v1/inspections/{id}/approve → mecánico aprueba
 *         PUT  /api/v1/inspections/{id}/reject  → mecánico rechaza
 */
@RestController
@RequestMapping("/api/v1/inspections")
@Tag(name = "Inspections", description = "Gestión de inspecciones y certificaciones técnicas")
public class InspectionController {

    private final InspectionCommandService inspectionCommandService;
    private final InspectionQueryService inspectionQueryService;

    public InspectionController(InspectionCommandService inspectionCommandService,
                                InspectionQueryService inspectionQueryService) {
        this.inspectionCommandService = inspectionCommandService;
        this.inspectionQueryService = inspectionQueryService;
    }

    /** US-11/13: Vendedor solicita inspección para su vehículo */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_SELLER')")
    @Operation(summary = "Solicitar inspección de vehículo")
    public ResponseEntity<InspectionResource> requestInspection(
            @RequestBody CreateInspectionResource resource,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        var command = new CreateInspectionCommand(
                resource.vehicleId(), currentUser.getId(), resource.scheduledAt());
        return inspectionCommandService.handle(command)
                .map(InspectionResourceFromEntityAssembler::toResourceFromEntity)
                .map(r -> new ResponseEntity<>(r, HttpStatus.CREATED))
                .orElse(ResponseEntity.badRequest().build());
    }

    /** US-12: Comprador ve informe de certificación de un vehículo */
    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Ver inspecciones de un vehículo")
    public ResponseEntity<List<InspectionResource>> getInspectionsByVehicle(@PathVariable Long vehicleId) {
        var inspections = inspectionQueryService
                .handle(new GetInspectionsByVehicleIdQuery(vehicleId))
                .stream()
                .map(InspectionResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(inspections);
    }

    /** US-13: Mecánico ve lista de inspecciones pendientes */
    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ROLE_MECHANIC')")
    @Operation(summary = "Ver solicitudes de inspección pendientes (mecánico)")
    public ResponseEntity<List<InspectionResource>> getPendingInspections() {
        var inspections = inspectionQueryService
                .handle(new GetPendingInspectionsQuery())
                .stream()
                .map(InspectionResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(inspections);
    }

    /** US-14: Mecánico se asigna a una inspección */
    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('ROLE_MECHANIC')")
    @Operation(summary = "Mecánico se asigna a una inspección")
    public ResponseEntity<InspectionResource> assignMechanic(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        var command = new AssignMechanicCommand(id, currentUser.getId());
        return inspectionCommandService.handle(command)
                .map(InspectionResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** US-15: Mecánico aprueba con certificado */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ROLE_MECHANIC')")
    @Operation(summary = "Aprobar inspección y generar certificado")
    public ResponseEntity<InspectionResource> approveInspection(
            @PathVariable Long id,
            @RequestBody ApproveInspectionResource resource,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        var command = new CompleteInspectionCommand(
                id, currentUser.getId(), resource.notes(), resource.certificateDetails());
        return inspectionCommandService.approve(command)
                .map(InspectionResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** US-15: Mecánico rechaza la inspección */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('ROLE_MECHANIC')")
    @Operation(summary = "Rechazar inspección")
    public ResponseEntity<InspectionResource> rejectInspection(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        return inspectionCommandService.reject(id, currentUser.getId(), body.get("notes"))
                .map(InspectionResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Ver detalle de una inspección */
    @GetMapping("/{id}")
    @Operation(summary = "Ver detalle de inspección")
    public ResponseEntity<InspectionResource> getInspectionById(@PathVariable Long id) {
        return inspectionQueryService.handle(new GetInspectionByIdQuery(id))
                .map(InspectionResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
