package com.pe.platform.userinteraction.domain.model.aggregates;

import com.pe.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;

/**
 * Favorite aggregate — User Interaction BC
 * US-08: Comprador guarda autos favoritos para revisarlos después
 */
@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"buyer_profile_id", "vehicle_id"}))
public class Favorite extends AuditableAbstractAggregateRoot<Favorite> {

    @Column(nullable = false, name = "buyer_profile_id")
    private Long buyerProfileId;

    @Column(nullable = false, name = "vehicle_id")
    private Long vehicleId;

    protected Favorite() {}

    public Favorite(Long buyerProfileId, Long vehicleId) {
        this.buyerProfileId = buyerProfileId;
        this.vehicleId = vehicleId;
    }
}
