package com.pe.platform.vehicle.application.internal.services.commandservices;

import com.pe.platform.vehicle.domain.model.commands.CreateVehicleCommand;
import com.pe.platform.vehicle.infrastructure.persistence.jpa.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleCommandServiceImplTest {

    @Mock private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleCommandServiceImpl vehicleCommandService;

    @Test
    void handle_CreateVehicleCommand_WhenPlateAlreadyExists_ShouldThrowException() {
        // Arrange
        var command = new CreateVehicleCommand(
                "Toyota", "Corolla", "2019", 12000.0,
                "Blanco", "Automatico", "1.8L", 45000.0,
                "4", "ABC-123", "Lima", "Buen estado",
                List.of(), "Gasolina", 180,
                "Luis", "999888777", "luis@email.com", 1L);

        when(vehicleRepository.existsByPlate("ABC-123")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> vehicleCommandService.handle(command));
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void handle_DeleteVehicle_WhenSellerDoesNotOwnVehicle_ShouldThrowSecurityException() {
        // Arrange
        var vehicle = mock(com.pe.platform.vehicle.domain.model.aggregates.Vehicle.class);
        when(vehicleRepository.findById(1L))
                .thenReturn(java.util.Optional.of(vehicle));
        when(vehicle.isOwnedBy(99L)).thenReturn(false);

        // Act & Assert
        assertThrows(SecurityException.class,
                () -> vehicleCommandService.deleteVehicle(1L, 99L));
    }
}