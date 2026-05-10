package com.autoapply.repository;

import com.autoapply.entity.JdExtractionCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JdExtractionCacheRepository extends JpaRepository<JdExtractionCache, UUID> {
    Optional<JdExtractionCache> findByJdHash(String jdHash);
}
