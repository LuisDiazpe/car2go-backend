package com.pe.platform.inspection.application.internal.services.commandservices;

import com.pe.platform.inspection.domain.model.aggregates.Inspection;
import com.pe.platform.inspection.domain.model.commands.AssignMechanicCommand;
import com.pe.platform.inspection.domain.model.commands.CompleteInspectionCommand;
import com.pe.platform.inspection.domain.model.commands.CreateInspectionCommand;
import com.pe.platform.inspection.domain.model.valueobjects.InspectionStatus;
import com.pe.platform.inspection.infrastructure.acl.vehicle.VehicleAclService;
import com.pe.platform.inspection.infrastructure.persistence.jpa.InspectionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios del servicio de comandos de Inspection
 * VehicleAclService (la llamada Feign a ms-vehicle),
 * para probar la logica
 */
@ExtendWith(MockitoExtension.class)
class InspectionCommandServiceImplTest {

    @Mock
    private InspectionRepository inspectionRepository;

    @Mock
    private VehicleAclService vehicleAclService;

    @InjectMocks
    private InspectionCommandServiceImpl inspectionCommandService;

    private Inspection inspeccionEnProgreso(Long mechanicId) {
        var insp = new Inspection(new CreateInspectionCommand(10L, 1L, LocalDateTime.now()));
        insp.assignMechanic(mechanicId); // queda IN_PROGRESS
        return insp;
    }

    @Test
    @DisplayName("Crear inspeccion la guarda con estado PENDING")
    void crearInspeccion_estadoPending() {
        var command = new CreateInspectionCommand(10L, 1L, LocalDateTime.now());
        when(inspectionRepository.save(any(Inspection.class))).thenAnswer(inv -> inv.getArgument(0));

        var resultado = inspectionCommandService.handle(command);

        assertTrue(resultado.isPresent());
        assertEquals(InspectionStatus.PENDING, resultado.get().getStatus());
    }

    @Test
    @DisplayName("Asignar mecanico cambia el estado a IN_PROGRESS")
    void asignarMecanico_estadoInProgress() {
        var insp = new Inspection(new CreateInspectionCommand(10L, 1L, LocalDateTime.now()));
        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(insp));
        when(inspectionRepository.save(any(Inspection.class))).thenAnswer(inv -> inv.getArgument(0));

        var resultado = inspectionCommandService.handle(new AssignMechanicCommand(1L, 7L));

        assertTrue(resultado.isPresent());
        assertEquals(InspectionStatus.IN_PROGRESS, resultado.get().getStatus());
    }

    @Test
    @DisplayName("Aprobar inspeccion la pone APPROVED y notifica a ms-vehicle (Feign)")
    void aprobar_notificaAVehicle() {
        var insp = inspeccionEnProgreso(7L);
        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(insp));
        when(inspectionRepository.save(any(Inspection.class))).thenAnswer(inv -> inv.getArgument(0));

        var command = new CompleteInspectionCommand(1L, 7L, "Todo OK", "CERT-001");
        var resultado = inspectionCommandService.approve(command);

        assertTrue(resultado.isPresent());
        assertEquals(InspectionStatus.APPROVED, resultado.get().getStatus());
        // Verifica que SI se llamo a ms-vehicle para marcar REVIEWED
        verify(vehicleAclService, times(1)).markReviewed(10L);
    }

    @Test
    @DisplayName("Aprobar por un mecanico no asignado lanza SecurityException y NO notifica")
    void aprobar_mecanicoNoAsignado() {
        var insp = inspeccionEnProgreso(7L); // asignada al mecanico 7
        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(insp));

        // intenta aprobar el mecanico 99 (no asignado)
        var command = new CompleteInspectionCommand(1L, 99L, "x", "y");

        assertThrows(SecurityException.class,
                () -> inspectionCommandService.approve(command));
        verify(vehicleAclService, never()).markReviewed(any());
    }

    @Test
    @DisplayName("Rechazar inspeccion la pone REJECTED y notifica a ms-vehicle")
    void rechazar_notificaAVehicle() {
        var insp = inspeccionEnProgreso(7L);
        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(insp));
        when(inspectionRepository.save(any(Inspection.class))).thenAnswer(inv -> inv.getArgument(0));

        var resultado = inspectionCommandService.reject(1L, 7L, "Motor con fallas");

        assertTrue(resultado.isPresent());
        assertEquals(InspectionStatus.REJECTED, resultado.get().getStatus());
        verify(vehicleAclService, times(1)).markRejected(10L);
    }
}
