package com.pe.platform.userinteraction.interfaces.rest;

import com.pe.platform.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.pe.platform.userinteraction.domain.model.aggregates.Favorite;
import com.pe.platform.userinteraction.infrastructure.persistence.jpa.FavoriteRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * US-08: Comprador guarda/elimina autos favoritos.
 */
@RestController
@RequestMapping("/api/v1/favorites")
@Tag(name = "Favorites", description = "Gestión de vehículos favoritos del comprador")
public class FavoriteController {

    private final FavoriteRepository favoriteRepository;

    public FavoriteController(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_BUYER')")
    @Operation(summary = "Mis vehículos favoritos")
    public ResponseEntity<List<Favorite>> getMyFavorites(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(favoriteRepository.findByBuyerProfileId(currentUser.getId()));
    }

    @PostMapping("/{vehicleId}")
    @PreAuthorize("hasAuthority('ROLE_BUYER')")
    @Operation(summary = "Agregar vehículo a favoritos")
    public ResponseEntity<Map<String, Object>> addFavorite(
            @PathVariable Long vehicleId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        if (favoriteRepository.existsByBuyerProfileIdAndVehicleId(currentUser.getId(), vehicleId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Vehicle already in favorites"));
        }
        var favorite = favoriteRepository.save(new Favorite(currentUser.getId(), vehicleId));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", favorite.getId(), "vehicleId", vehicleId));
    }

    @DeleteMapping("/{vehicleId}")
    @PreAuthorize("hasAuthority('ROLE_BUYER')")
    @Operation(summary = "Eliminar vehículo de favoritos")
    public ResponseEntity<Void> removeFavorite(
            @PathVariable Long vehicleId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        return favoriteRepository
                .findByBuyerProfileIdAndVehicleId(currentUser.getId(), vehicleId)
                .map(fav -> {
                    favoriteRepository.delete(fav);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().<Void>build());
    }
}
