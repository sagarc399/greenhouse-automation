package com.greenhouse.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing a system user authenticated via OAuth2 (Google).
 *
 * <p>Users are assigned a {@link UserRole} that controls access to API endpoints:
 * <ul>
 *   <li>{@link UserRole#ADMIN} — full access to all resources.</li>
 *   <li>{@link UserRole#OPERATOR} — read access + sensor readings + alert updates.</li>
 * </ul>
 * </p>
 *
 * @see UserRole
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    /**
     * Auto-generated primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Full display name from the OAuth2 provider.
     */
    @NotBlank
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Email address from the OAuth2 provider — used as unique identifier.
     */
    @Email
    @NotBlank
    @Column(nullable = false, unique = true, length = 200)
    private String email;

    /**
     * URL to the user's profile picture from the OAuth2 provider.
     */
    @Column(length = 500)
    private String pictureUrl;

    /**
     * OAuth2 provider name (e.g., "google").
     */
    @Column(length = 50)
    private String provider;

    /**
     * Unique subject ID from the OAuth2 provider.
     */
    @Column(length = 200)
    private String providerId;

    /**
     * Application-level role controlling endpoint access.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserRole role = UserRole.OPERATOR;

    /**
     * Timestamp when this record was created.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of the last update to this record.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Sets audit timestamps before persisting.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the audit timestamp before merging.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Application roles that govern endpoint access permissions.
     */
    public enum UserRole {
        /** Full administrative access to all endpoints. */
        ADMIN,
        /** Read access plus sensor reading creation and alert updates. */
        OPERATOR
    }
}
