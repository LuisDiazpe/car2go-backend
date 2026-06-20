package com.pe.platform.userinteraction.domain.model.aggregates;

import com.pe.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;

/**
 * Review aggregate — User Interaction BC (Sprint 3)
 * Reseña con estrellas (1-5) de un usuario hacia otro.
 * Regla de negocio: solo puede reseñar quien tiene al menos 1 transacción
 * (se valida en el controller consultando a ms-payment).
 * Un usuario solo puede dejar UNA reseña por cada usuario objetivo (UniqueConstraint).
 */
@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"author_profile_id", "target_profile_id"}))
public class Review extends AuditableAbstractAggregateRoot<Review> {

    @Column(nullable = false, name = "author_profile_id")
    private Long authorProfileId;

    @Column(nullable = false, name = "author_username")
    private String authorUsername;

    @Column(nullable = false, name = "target_profile_id")
    private Long targetProfileId;

    /** Estrellas de 1 a 5 */
    @Column(nullable = false)
    private Integer rating;

    @Column(length = 500)
    private String comment;

    protected Review() {}

    public Review(Long authorProfileId, String authorUsername, Long targetProfileId, Integer rating, String comment) {
        this.authorProfileId = authorProfileId;
        this.authorUsername = authorUsername;
        this.targetProfileId = targetProfileId;
        this.rating = rating;
        this.comment = comment;
    }

    public void update(Integer rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }
}
