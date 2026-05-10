package com.autoapply.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ats_scores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtsScore {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "jd_hash", nullable = false, length = 64)
    private String jdHash;

    @Column(name = "jd_text_snippet", length = 500)
    private String jdTextSnippet;

    @Column(name = "total_score", nullable = false)
    private Short totalScore;

    @Column(name = "max_score", nullable = false)
    @Builder.Default
    private Short maxScore = 100;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "category_breakdown", nullable = false, columnDefinition = "jsonb")
    private String categoryBreakdown;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = OffsetDateTime.now();
    }
}
