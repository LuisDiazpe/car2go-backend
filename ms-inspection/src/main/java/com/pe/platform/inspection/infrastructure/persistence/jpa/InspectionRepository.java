package com.pe.platform.inspection.infrastructure.persistence.jpa;

import com.pe.platform.inspection.domain.model.aggregates.Inspection;
import com.pe.platform.inspection.domain.model.valueobjects.InspectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InspectionRepository extends JpaRepository<Inspection, Long> {
    List<Inspection> findByVehicleId(Long vehicleId);
    List<Inspection> findByStatus(InspectionStatus status);
    List<Inspection> findByMechanicProfileId(Long mechanicProfileId);
    List<Inspection> findByMechanicProfileIdAndStatus(Long mechanicProfileId, InspectionStatus status);
}
