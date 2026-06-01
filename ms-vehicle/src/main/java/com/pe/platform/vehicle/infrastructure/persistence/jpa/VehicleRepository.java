package com.pe.platform.vehicle.infrastructure.persistence.jpa;

import com.pe.platform.vehicle.domain.model.aggregates.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findBySellerProfileId(Long sellerProfileId);
    List<Vehicle> findByLocationContainingIgnoreCase(String location);
    boolean existsByPlate(String plate);
}
