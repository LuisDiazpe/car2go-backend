package com.pe.platform.vehicle.domain.model.aggregates;

import com.pe.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.pe.platform.vehicle.common.converters.ListToStringConverter;
import com.pe.platform.vehicle.domain.model.commands.CreateVehicleCommand;
import com.pe.platform.vehicle.domain.model.commands.UpdateVehicleCommand;
import com.pe.platform.vehicle.domain.model.valueobjects.VehicleStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

/**
 * Vehicle aggregate root — Vehicle Management Bounded Context.
 *
 * US-03: Vendedor lista un auto (crea vehículo con estado PENDING)
 * US-04: Vendedor edita su listado
 * US-05: Vendedor elimina su listado
 * US-06: Comprador ve detalles del auto
 * US-08: Comprador guarda favoritos (relación en UserInteraction BC)
 */
@Getter
@Entity
public class Vehicle extends AuditableAbstractAggregateRoot<Vehicle> {

    @Column(nullable = false, length = 100)
    private String brand;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(nullable = false, length = 10)
    private String year;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false, length = 20)
    private String color;

    @Column(nullable = false, length = 20)
    private String transmission;

    @Column(nullable = false, length = 50)
    private String engine;

    @Column(nullable = false)
    private double mileage;

    @Column(nullable = false, length = 5)
    private String doors;

    @Column(nullable = false, unique = true, length = 10)
    private String plate;

    @Column(nullable = false, length = 100)
    private String location;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Convert(converter = ListToStringConverter.class)
    @Column(name = "images", columnDefinition = "LONGTEXT")
    private List<String> images;

    @Column(nullable = false, length = 20)
    private String fuel;

    @Column(nullable = false)
    private int topSpeed;

    /** Seller contact info embedded in vehicle for quick reference */
    @Column(nullable = false, length = 100)
    private String contactName;

    @Column(nullable = false, length = 20)
    private String contactPhone;

    @Column(nullable = false, length = 100)
    private String contactEmail;

    /** FK to Profile in UserInteraction BC (anti-corruption layer via ID) */
    @Column(nullable = false)
    private Long sellerProfileId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleStatus status;

    protected Vehicle() {}

    public Vehicle(CreateVehicleCommand command) {
        this.brand = command.brand();
        this.model = command.model();
        this.year = command.year();
        this.price = command.price();
        this.color = command.color();
        this.transmission = command.transmission();
        this.engine = command.engine();
        this.mileage = command.mileage();
        this.doors = command.doors();
        this.plate = command.plate();
        this.location = command.location();
        this.description = command.description();
        this.images = command.images();
        this.fuel = command.fuel();
        this.topSpeed = command.topSpeed();
        this.contactName = command.contactName();
        this.contactPhone = command.contactPhone();
        this.contactEmail = command.contactEmail();
        this.sellerProfileId = command.sellerProfileId();
        this.status = VehicleStatus.PENDING;
    }

    public Vehicle update(UpdateVehicleCommand command) {
        this.brand = command.brand();
        this.model = command.model();
        this.year = command.year();
        this.price = command.price();
        this.color = command.color();
        this.transmission = command.transmission();
        this.engine = command.engine();
        this.mileage = command.mileage();
        this.doors = command.doors();
        this.location = command.location();
        this.description = command.description();
        this.images = command.images();
        this.fuel = command.fuel();
        this.topSpeed = command.topSpeed();
        this.contactName = command.contactName();
        this.contactPhone = command.contactPhone();
        this.contactEmail = command.contactEmail();
        return this;
    }

    public void markAsReviewed() {
        this.status = VehicleStatus.REVIEWED;
    }

    public void markAsRejected() {
        this.status = VehicleStatus.REJECTED;
    }

    public void markAsSold() {
        this.status = VehicleStatus.SOLD;
    }

    public boolean isOwnedBy(Long profileId) {
        return this.sellerProfileId.equals(profileId);
    }
}
