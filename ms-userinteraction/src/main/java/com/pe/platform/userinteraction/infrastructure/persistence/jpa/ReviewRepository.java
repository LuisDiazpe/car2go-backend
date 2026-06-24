package com.pe.platform.userinteraction.infrastructure.persistence.jpa;

import com.pe.platform.userinteraction.domain.model.aggregates.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByTargetProfileIdOrderByCreatedAtDesc(Long targetProfileId);
    Optional<Review> findByAuthorProfileIdAndTargetProfileId(Long authorProfileId, Long targetProfileId);
    boolean existsByAuthorProfileIdAndTargetProfileId(Long authorProfileId, Long targetProfileId);
}
