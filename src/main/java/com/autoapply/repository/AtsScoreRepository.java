package com.autoapply.repository;

import com.autoapply.entity.AtsScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AtsScoreRepository extends JpaRepository<AtsScore, UUID> {
    List<AtsScore> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<AtsScore> findByIdAndUserId(UUID id, UUID userId);
}
