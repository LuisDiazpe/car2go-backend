package com.pe.platform.userinteraction.infrastructure.persistence.jpa;

import com.pe.platform.userinteraction.domain.model.aggregates.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByBuyerProfileId(Long buyerProfileId);
    Optional<Favorite> findByBuyerProfileIdAndVehicleId(Long buyerProfileId, Long vehicleId);
    boolean existsByBuyerProfileIdAndVehicleId(Long buyerProfileId, Long vehicleId);
}
