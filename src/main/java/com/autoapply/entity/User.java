package com.autoapply.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String provider;

    @Column(name = "provider_id", nullable = false, length = 255)
    private String providerId;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(length = 255)
    private String name;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "target_roles", columnDefinition = "text[]")
    @Builder.Default
    private String[] targetRoles = new String[0];

    @Column(name = "years_experience")
    private Short yearsExperience;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "preferred_locations", columnDefinition = "text[]")
    @Builder.Default
    private String[] preferredLocations = new String[0];

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "preferred_skills", columnDefinition = "text[]")
    @Builder.Default
    private String[] preferredSkills = new String[0];

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "last_login_at", nullable = false)
    private OffsetDateTime lastLoginAt;

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.lastLoginAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
