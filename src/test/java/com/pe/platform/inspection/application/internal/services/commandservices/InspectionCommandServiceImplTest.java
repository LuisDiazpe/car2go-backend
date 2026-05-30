package com.pe.platform.inspection.application.internal.services.commandservices;

import com.pe.platform.inspection.domain.model.aggregates.Inspection;
import com.pe.platform.inspection.domain.model.commands.CompleteInspectionCommand;
import com.pe.platform.inspection.infrastructure.persistence.jpa.InspectionRepository;
import com.pe.platform.vehicle.domain.model.queries.GetVehicleByIdQuery;
import com.pe.platform.vehicle.domain.services.VehicleQueryService;
import com.pe.platform.vehicle.infrastructure.persistence.jpa.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InspectionCommandServiceImplTest {

    @Mock private InspectionRepository inspectionRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private VehicleQueryService vehicleQueryService;

    @InjectMocks
    private InspectionCommandServiceImpl inspectionCommandService;

    @Test
    void approve_WhenMechanicNotAssigned_ShouldThrowSecurityException() {
        // Arrange
        var inspection = mock(Inspection.class);
        var command = new CompleteInspectionCommand(1L, 99L, "notas", "certificado");

        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(inspection));
        when(inspection.isAssignedTo(99L)).thenReturn(false);

        // Act & Assert
        assertThrows(SecurityException.class,
                () -> inspectionCommandService.approve(command));
    }

    @Test
    void approve_WhenMechanicIsAssigned_ShouldApproveAndUpdateVehicle() {
        // Arrange
        var inspection = mock(Inspection.class);
        var vehicle = mock(com.pe.platform.vehicle.domain.model.aggregates.Vehicle.class);
        var command = new CompleteInspectionCommand(1L, 5L, "Motor OK", "Cert-001");

        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(inspection));
        when(inspection.isAssignedTo(5L)).thenReturn(true);
        when(inspection.getVehicleId()).thenReturn(10L);
        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(vehicle));
        when(inspectionRepository.save(any())).thenReturn(inspection);

        // Act
        var result = inspectionCommandService.approve(command);

        // Assert
        assertTrue(result.isPresent());
        verify(inspection, times(1)).approve(command);
        verify(vehicle, times(1)).markAsReviewed();
        verify(vehicleRepository, times(1)).save(vehicle);
    }
}