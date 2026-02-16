package net.jrodolfo.jobportal.model;

import net.jrodolfo.jobportal.constant.AuthProvider;
import net.jrodolfo.jobportal.constant.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users") // Avoid using "user" as it's a reserved keyword in MySQL
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "User account representation.")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique user id.", example = "1")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Display name.", example = "Rod Oliveira")
    private String name; // Needed for Google users

    @Column(nullable = false, unique = true)
    @Schema(description = "Unique user email.", example = "user@test.com")
    private String email;

    @Column(nullable = true) // OAuth users won't have passwords
    @Schema(description = "Password for local users.", example = "user123")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Authentication provider.", example = "LOCAL")
    private AuthProvider authProvider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Authorization role.", example = "APPLICANT")
    private Role role;

    @Column(name = "created_at", nullable = false)
    @Schema(description = "Date-time when the user record was created.", example = "2026-02-14T12:30:00")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @Schema(description = "Date-time when the user record was last updated.", example = "2026-02-14T12:45:00")
    private LocalDateTime updatedAt;

    /* Constructor for Normal Users (Requires Password) */
    public User(String name, String email, String password, AuthProvider provider, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.authProvider = provider;
        this.role = role;
    }

    /* Constructor for Google Users (No Password) */
    public User(String name, String email, AuthProvider provider, Role role) {
        this.name = name;
        this.email = email;
        this.password = null; // Google Users Don't Have Passwords
        this.authProvider = provider;
        this.role = role;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
