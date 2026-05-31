package com.pe.platform.userinteraction.interfaces.rest;

import com.pe.platform.shared.infrastructure.security.CurrentUser;
import com.pe.platform.userinteraction.domain.model.aggregates.Favorite;
import com.pe.platform.userinteraction.infrastructure.persistence.jpa.FavoriteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios del controlador de Favoritos (User Interaction BC)
 * Mockea el repositorio para probar la logica
 */
@ExtendWith(MockitoExtension.class)
class FavoriteControllerTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @InjectMocks
    private FavoriteController favoriteController;

    private CurrentUser comprador;

    @BeforeEach
    void setUp() {
        comprador = new CurrentUser(8L, "comprador15", "ROLE_BUYER");
    }

    @Test
    @DisplayName("Agregar favorito nuevo devuelve 201 CREATED")
    void agregarFavorito_nuevo_created() {
        when(favoriteRepository.existsByBuyerProfileIdAndVehicleId(8L, 3L)).thenReturn(false);
        // El controller usa favorite.getId() en Map.of(), que no admite null.
        // Por eso el favorito guardado debe devolver un id (lo simula la BD con un mock).
        Favorite guardado = mock(Favorite.class);
        when(guardado.getId()).thenReturn(1L);
        when(favoriteRepository.save(any(Favorite.class))).thenReturn(guardado);

        var response = favoriteController.addFavorite(3L, comprador);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(favoriteRepository, times(1)).save(any(Favorite.class));
    }

    @Test
    @DisplayName("Agregar un favorito que ya existe devuelve 409 CONFLICT")
    void agregarFavorito_duplicado_conflict() {
        when(favoriteRepository.existsByBuyerProfileIdAndVehicleId(8L, 3L)).thenReturn(true);

        var response = favoriteController.addFavorite(3L, comprador);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    @Test
    @DisplayName("Listar favoritos devuelve la lista del comprador")
    void listarFavoritos_ok() {
        when(favoriteRepository.findByBuyerProfileId(8L))
                .thenReturn(List.of(new Favorite(8L, 3L), new Favorite(8L, 5L)));

        var response = favoriteController.getMyFavorites(comprador);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("Eliminar un favorito existente devuelve 204 NO CONTENT")
    void eliminarFavorito_existente_noContent() {
        when(favoriteRepository.findByBuyerProfileIdAndVehicleId(8L, 3L))
                .thenReturn(Optional.of(new Favorite(8L, 3L)));

        var response = favoriteController.removeFavorite(3L, comprador);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(favoriteRepository, times(1)).delete(any(Favorite.class));
    }

    @Test
    @DisplayName("Eliminar un favorito inexistente devuelve 404 NOT FOUND")
    void eliminarFavorito_inexistente_notFound() {
        when(favoriteRepository.findByBuyerProfileIdAndVehicleId(8L, 99L))
                .thenReturn(Optional.empty());

        var response = favoriteController.removeFavorite(99L, comprador);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(favoriteRepository, never()).delete(any(Favorite.class));
    }
}
