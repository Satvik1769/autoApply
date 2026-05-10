package com.autoapply.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "jd_extraction_cache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JdExtractionCache {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "jd_hash", nullable = false, unique = true, length = 64)
    private String jdHash;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extracted_json", nullable = false, columnDefinition = "jsonb")
    private String extractedJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "last_accessed_at", nullable = false)
    private OffsetDateTime lastAccessedAt;

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.lastAccessedAt = now;
    }
}
