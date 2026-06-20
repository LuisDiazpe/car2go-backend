package com.pe.platform.userinteraction.interfaces.rest;

import com.pe.platform.shared.infrastructure.security.CurrentUser;
import com.pe.platform.userinteraction.domain.model.aggregates.Review;
import com.pe.platform.userinteraction.infrastructure.acl.PaymentClient;
import com.pe.platform.userinteraction.infrastructure.persistence.jpa.ReviewRepository;
import com.pe.platform.userinteraction.interfaces.rest.resources.CreateReviewResource;
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
 * Sprint 3 — Reseñas (estrellas 1-5).
 * Regla de negocio: solo puede reseñar quien tiene al menos 1 transacción
 * (se valida consultando a ms-payment vía Feign — comunicación entre microservicios).
 * Un usuario solo puede tener UNA reseña por cada usuario objetivo.
 */
@RestController
@RequestMapping("/api/v1/reviews")
@Tag(name = "Reviews", description = "Reseñas con calificación entre usuarios")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final PaymentClient paymentClient;

    public ReviewController(ReviewRepository reviewRepository, PaymentClient paymentClient) {
        this.reviewRepository = reviewRepository;
        this.paymentClient = paymentClient;
    }

    /** Reseñas de un usuario + su promedio de estrellas. Lectura pública. */
    @GetMapping("/user/{targetProfileId}")
    @Operation(summary = "Ver reseñas y promedio de un usuario")
    public ResponseEntity<Map<String, Object>> getReviewsForUser(@PathVariable Long targetProfileId) {
        List<Review> reviews = reviewRepository.findByTargetProfileIdOrderByCreatedAtDesc(targetProfileId);
        double average = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        return ResponseEntity.ok(Map.of(
                "targetProfileId", targetProfileId,
                "average", Math.round(average * 10.0) / 10.0,  // 1 decimal (ej. 4.3)
                "count", reviews.size(),
                "reviews", reviews
        ));
    }

    /** Crear o actualizar una reseña. Requiere tener al menos 1 transacción. */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Dejar una reseña (requiere al menos 1 transacción)")
    public ResponseEntity<?> createReview(
            @RequestBody CreateReviewResource resource,
            @AuthenticationPrincipal CurrentUser currentUser) {

        // Validación de estrellas
        if (resource.rating() == null || resource.rating() < 1 || resource.rating() > 5) {
            return ResponseEntity.badRequest().body(Map.of("message", "Rating must be between 1 and 5"));
        }

        // Regla de negocio: el autor debe tener al menos 1 transacción (consulta a ms-payment vía Feign)
        try {
            var result = paymentClient.countTransactions(currentUser.getId());
            boolean hasTransactions = Boolean.TRUE.equals(result.get("hasTransactions"));
            if (!hasTransactions) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "You need at least 1 transaction to leave a star rating"));
            }
        } catch (Exception e) {
            // Si ms-payment no responde, por resiliencia se rechaza la reseña de forma controlada
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "Could not verify transactions, try again later"));
        }

        // Si ya existe reseña de este autor a este objetivo, se actualiza; si no, se crea
        var existing = reviewRepository.findByAuthorProfileIdAndTargetProfileId(
                currentUser.getId(), resource.targetProfileId());

        Review review;
        if (existing.isPresent()) {
            review = existing.get();
            review.update(resource.rating(), resource.comment());
        } else {
            review = new Review(currentUser.getId(), currentUser.getUsername(),
                    resource.targetProfileId(), resource.rating(), resource.comment());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewRepository.save(review));
    }
}
