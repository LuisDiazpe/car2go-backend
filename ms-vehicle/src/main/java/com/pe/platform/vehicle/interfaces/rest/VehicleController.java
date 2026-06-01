package com.pe.platform.vehicle.interfaces.rest;

import com.pe.platform.shared.infrastructure.security.CurrentUser;
import com.pe.platform.vehicle.domain.model.queries.*;
import com.pe.platform.vehicle.domain.services.VehicleCommandService;
import com.pe.platform.vehicle.domain.services.VehicleQueryService;
import com.pe.platform.vehicle.interfaces.rest.resources.*;
import com.pe.platform.vehicle.interfaces.rest.transform.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Vehicle REST controller — Vehicle Management BC.
 * US-03: Vendedor lista auto
 * US-04: Vendedor edita auto
 * US-05: Vendedor elimina auto
 * US-06: Comprador ve detalles
 * US-07: Comparar autos (devuelve lista filtrada)
 */
@RestController
@RequestMapping("/api/v1/vehicles")
@Tag(name = "Vehicles", description = "Gestión del catálogo de vehículos")
public class VehicleController {

    private final VehicleCommandService vehicleCommandService;
    private final VehicleQueryService vehicleQueryService;

    public VehicleController(VehicleCommandService vehicleCommandService,
                             VehicleQueryService vehicleQueryService) {
        this.vehicleCommandService = vehicleCommandService;
        this.vehicleQueryService = vehicleQueryService;
    }

    /** US-06: catálogo público — sin autenticación */
    @GetMapping
    @Operation(summary = "Obtener todos los vehículos disponibles")
    public ResponseEntity<List<VehicleResource>> getAllVehicles() {
        var vehicles = vehicleQueryService.handle(new GetAllVehiclesQuery())
                .stream()
                .map(VehicleResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(vehicles);
    }

    /** US-06: ver detalle de un auto */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener vehículo por ID")
    public ResponseEntity<VehicleResource> getVehicleById(@PathVariable Long id) {
        return vehicleQueryService.handle(new GetVehicleByIdQuery(id))
                .map(VehicleResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Filtrar por ubicación */
    @GetMapping("/location/{location}")
    @Operation(summary = "Buscar vehículos por ubicación")
    public ResponseEntity<List<VehicleResource>> getVehiclesByLocation(@PathVariable String location) {
        var vehicles = vehicleQueryService.handle(new GetVehiclesByLocationQuery(location))
                .stream()
                .map(VehicleResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(vehicles);
    }

    /** US-03: Vendedor lista su auto — solo ROLE_SELLER */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_SELLER')")
    @Operation(summary = "Publicar nuevo vehículo (solo vendedor)")
    public ResponseEntity<VehicleResource> createVehicle(
            @Valid @RequestBody CreateVehicleResource resource,
            @AuthenticationPrincipal CurrentUser currentUser) {

        var command = CreateVehicleCommandFromResourceAssembler
                .toCommandFromResource(resource, currentUser.getId());
        return vehicleCommandService.handle(command)
                .map(VehicleResourceFromEntityAssembler::toResourceFromEntity)
                .map(r -> new ResponseEntity<>(r, HttpStatus.CREATED))
                .orElse(ResponseEntity.badRequest().build());
    }

    /** US-04: Vendedor edita su propio auto */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SELLER')")
    @Operation(summary = "Actualizar vehículo (solo el vendedor propietario)")
    public ResponseEntity<VehicleResource> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVehicleResource resource,
            @AuthenticationPrincipal CurrentUser currentUser) {

        // Verificar que el vehículo pertenece al seller autenticado
        var existingOpt = vehicleQueryService.handle(new GetVehicleByIdQuery(id));
        if (existingOpt.isEmpty()) return ResponseEntity.notFound().build();
        if (!existingOpt.get().isOwnedBy(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var command = UpdateVehicleCommandFromResourceAssembler.toCommandFromResource(id, resource);
        return vehicleCommandService.handle(command)
                .map(VehicleResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** US-05: Vendedor elimina su auto */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SELLER')")
    @Operation(summary = "Eliminar vehículo (solo el vendedor propietario)")
    public ResponseEntity<Void> deleteVehicle(
            @PathVariable Long id,
            @AuthenticationPrincipal CurrentUser currentUser) {
        try {
            vehicleCommandService.deleteVehicle(id, currentUser.getId());
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** Autos del vendedor autenticado */
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('ROLE_SELLER')")
    @Operation(summary = "Mis vehículos publicados")
    public ResponseEntity<List<VehicleResource>> getMyVehicles(
            @AuthenticationPrincipal CurrentUser currentUser) {
        var vehicles = vehicleQueryService
                .handle(new GetVehiclesBySellerProfileIdQuery(currentUser.getId()))
                .stream()
                .map(VehicleResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(vehicles);
    }

    // Endpoints internos (service-to-service): ms-inspection los llama

    /** Llamado por ms-inspection cuando el mecanico APRUEBA: PENDING -> REVIEWED */
    @PutMapping("/{id}/mark-reviewed")
    @Operation(summary = "[Interno] Marcar vehiculo como REVIEWED (llamado por ms-inspection)")
    public ResponseEntity<Void> markReviewed(@PathVariable Long id) {
        try {
            vehicleCommandService.markReviewed(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** Llamado por ms-inspection cuando el mecanico RECHAZA: PENDING -> REJECTED */
    @PutMapping("/{id}/mark-rejected")
    @Operation(summary = "[Interno] Marcar vehiculo como REJECTED (llamado por ms-inspection)")
    public ResponseEntity<Void> markRejected(@PathVariable Long id) {
        try {
            vehicleCommandService.markRejected(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
