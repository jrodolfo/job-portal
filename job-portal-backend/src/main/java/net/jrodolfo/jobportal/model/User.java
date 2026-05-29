package net.jrodolfo.jobportal.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.jrodolfo.jobportal.constant.AuthProvider;
import net.jrodolfo.jobportal.constant.Role;

import java.time.LocalDateTime;

/**
 * Represents a user account in the system.
 * This entity supports both local authentication and OAuth (Google) authentication.
 */
@Entity
@Table(name = "users") // Avoid using "user" as it's a reserved keyword in MySQL
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "User account representation.")
public class User {

    /**
     * The unique identifier for the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique user id.", example = "1")
    private Long id;

    /**
     * The display name of the user.
     */
    @Column(nullable = false)
    @Schema(description = "Display name.", example = "Rod Oliveira")
    private String name; // Needed for Google users

    /**
     * The unique email address of the user, used for login.
     */
    @Column(nullable = false, unique = true)
    @Schema(description = "Unique user email.", example = "user@test.com")
    private String email;

    /**
     * The hashed password for local users.
     * Null for OAuth users.
     */
    @Column(nullable = true) // OAuth users won't have passwords
    @Schema(description = "Password for local users.", example = "user123")
    private String password;

    /**
     * The authentication provider used by the user.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Authentication provider.", example = "LOCAL")
    private AuthProvider authProvider;

    /**
     * The role assigned to the user for authorization purposes.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Authorization role.", example = "APPLICANT")
    private Role role;

    /**
     * The timestamp when the user record was created.
     */
    @Column(name = "created_at", nullable = false)
    @Schema(description = "Date-time when the user record was created.", example = "2026-02-14T12:30:00")
    private LocalDateTime createdAt;

    /**
     * The timestamp when the user record was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    @Schema(description = "Date-time when the user record was last updated.", example = "2026-02-14T12:45:00")
    private LocalDateTime updatedAt;

    /**
     * Constructs a new User with local authentication (requires a password).
     *
     * @param name     the user's name
     * @param email    the user's email
     * @param password the user's hashed password
     * @param provider the authentication provider (typically LOCAL)
     * @param role     the user's role
     */
    public User(String name, String email, String password, AuthProvider provider, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.authProvider = provider;
        this.role = role;
    }

    /**
     * Constructs a new User for OAuth authentication (no password required).
     *
     * @param name     the user's name
     * @param email    the user's email
     * @param provider the authentication provider (e.g., GOOGLE)
     * @param role     the user's role
     */
    public User(String name, String email, AuthProvider provider, Role role) {
        this.name = name;
        this.email = email;
        this.password = null; // Google Users Don't Have Passwords
        this.authProvider = provider;
        this.role = role;
    }

    /**
     * Lifecycle callback to set the creation and update timestamps before the entity is persisted.
     */
    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    /**
     * Lifecycle callback to update the update timestamp before the entity is updated.
     */
    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
