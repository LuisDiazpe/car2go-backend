package com.pe.platform.userinteraction.interfaces.rest;

import com.pe.platform.shared.infrastructure.security.CurrentUser;
import com.pe.platform.userinteraction.domain.model.aggregates.Comment;
import com.pe.platform.userinteraction.infrastructure.persistence.jpa.CommentRepository;
import com.pe.platform.userinteraction.interfaces.rest.resources.CreateCommentResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Sprint 3 — Comentarios.
 * Cualquier usuario autenticado (cualquier rol) puede comentar a cualquier otro.
 */
@RestController
@RequestMapping("/api/v1/comments")
@Tag(name = "Comments", description = "Comentarios entre usuarios")
public class CommentController {

    private final CommentRepository commentRepository;

    public CommentController(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /** Comentarios dirigidos a un usuario (su muro). Público de lectura. */
    @GetMapping("/user/{targetProfileId}")
    @Operation(summary = "Ver comentarios de un usuario")
    public ResponseEntity<List<Comment>> getCommentsForUser(@PathVariable Long targetProfileId) {
        return ResponseEntity.ok(
                commentRepository.findByTargetProfileIdOrderByCreatedAtDesc(targetProfileId));
    }

    /** Crear un comentario. Cualquier rol autenticado. */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Dejar un comentario a otro usuario")
    public ResponseEntity<Comment> createComment(
            @RequestBody CreateCommentResource resource,
            @AuthenticationPrincipal CurrentUser currentUser) {

        var comment = new Comment(
                currentUser.getId(),
                currentUser.getUsername(),
                resource.targetProfileId(),
                resource.content()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(commentRepository.save(comment));
    }
}
