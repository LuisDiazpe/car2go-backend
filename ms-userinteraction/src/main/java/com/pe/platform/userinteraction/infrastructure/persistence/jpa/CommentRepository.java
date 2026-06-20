package com.pe.platform.userinteraction.infrastructure.persistence.jpa;

import com.pe.platform.userinteraction.domain.model.aggregates.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Comentarios dirigidos a un usuario (su "muro"), más recientes primero
    List<Comment> findByTargetProfileIdOrderByCreatedAtDesc(Long targetProfileId);
}
