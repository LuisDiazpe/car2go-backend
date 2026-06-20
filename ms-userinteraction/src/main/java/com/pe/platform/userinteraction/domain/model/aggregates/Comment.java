package com.pe.platform.userinteraction.domain.model.aggregates;

import com.pe.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;

/**
 * Comment aggregate — User Interaction BC (Sprint 3)
 * Cualquier usuario (cualquier rol) puede dejar un comentario de texto libre
 * a cualquier otro usuario, sin restricciones.
 */
@Getter
@Entity
public class Comment extends AuditableAbstractAggregateRoot<Comment> {

    /** Quién escribe el comentario */
    @Column(nullable = false, name = "author_profile_id")
    private Long authorProfileId;

    @Column(nullable = false, name = "author_username")
    private String authorUsername;

    /** A quién va dirigido el comentario */
    @Column(nullable = false, name = "target_profile_id")
    private Long targetProfileId;

    @Column(nullable = false, length = 1000)
    private String content;

    protected Comment() {}

    public Comment(Long authorProfileId, String authorUsername, Long targetProfileId, String content) {
        this.authorProfileId = authorProfileId;
        this.authorUsername = authorUsername;
        this.targetProfileId = targetProfileId;
        this.content = content;
    }
}
