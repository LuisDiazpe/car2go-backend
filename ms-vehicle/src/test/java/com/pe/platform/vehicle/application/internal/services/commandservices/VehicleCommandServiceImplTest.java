package com.pe.platform.vehicle.application.internal.services.commandservices;

import com.pe.platform.vehicle.domain.model.aggregates.Vehicle;
import com.pe.platform.vehicle.domain.model.commands.CreateVehicleCommand;
import com.pe.platform.vehicle.domain.model.valueobjects.VehicleStatus;
import com.pe.platform.vehicle.infrastructure.persistence.jpa.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios del servicio de comandos de Vehicle
 *
 */
@ExtendWith(MockitoExtension.class)
class VehicleCommandServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleCommandServiceImpl vehicleCommandService;

    private CreateVehicleCommand crearComando(String plate) {
        return new CreateVehicleCommand(
                "Toyota", "Corolla", "2020", 15000.0, "Rojo",
                "Manual", "1.8L", 50000.0, "4", plate, "Lima",
                "Buen auto", List.of("http://img.com/1.jpg"), "Gasolina",
                180, "Juan", "999888777", "juan@test.com", 1L
        );
    }

    @Test
    @DisplayName("Crear vehiculo guarda y devuelve el vehiculo con estado PENDING")
    void crearVehiculo_exitoso() {
        var command = crearComando("ABC-123");
        when(vehicleRepository.existsByPlate("ABC-123")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Vehicle> resultado = vehicleCommandService.handle(command);

        assertTrue(resultado.isPresent());
        assertEquals(VehicleStatus.PENDING, resultado.get().getStatus());
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Crear vehiculo con placa duplicada lanza excepcion")
    void crearVehiculo_placaDuplicada() {
        var command = crearComando("XYZ-789");
        when(vehicleRepository.existsByPlate("XYZ-789")).thenReturn(true);

        var ex = assertThrows(IllegalArgumentException.class,
                () -> vehicleCommandService.handle(command));
        assertTrue(ex.getMessage().contains("already exists"));
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("markReviewed cambia el estado del vehiculo a REVIEWED")
    void markReviewed_cambiaEstado() {
        var vehicle = new Vehicle(crearComando("ABC-123"));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        vehicleCommandService.markReviewed(1L);

        assertEquals(VehicleStatus.REVIEWED, vehicle.getStatus());
        verify(vehicleRepository).save(vehicle);
    }

    @Test
    @DisplayName("markRejected cambia el estado del vehiculo a REJECTED")
    void markRejected_cambiaEstado() {
        var vehicle = new Vehicle(crearComando("ABC-123"));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        vehicleCommandService.markRejected(1L);

        assertEquals(VehicleStatus.REJECTED, vehicle.getStatus());
        verify(vehicleRepository).save(vehicle);
    }

    @Test
    @DisplayName("markReviewed sobre un vehiculo inexistente lanza excepcion")
    void markReviewed_vehiculoNoExiste() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> vehicleCommandService.markReviewed(99L));
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }
}
