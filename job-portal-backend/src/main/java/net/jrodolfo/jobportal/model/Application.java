package net.jrodolfo.jobportal.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.jrodolfo.jobportal.constant.ApplicationStatus;

import java.time.LocalDateTime;

/**
 * Represents a job application made by a user for a specific job.
 * This entity links a {@link User} to a {@link Job} and tracks the application status.
 * It enforces a unique constraint so that a user can only apply once to the same job.
 */
@Entity
@Table(
        name = "applications",
        uniqueConstraints = @UniqueConstraint(name = "uk_application_user_job", columnNames = {"user_id", "job_id"})
)
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Application linking one user to one job.")
public class Application {

    /**
     * The unique identifier for the application.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique application id.", example = "100")
    private Long id;

    /**
     * The user who submitted the application.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "Applicant user.")
    private User user;

    /**
     * The job for which the application was submitted.
     */
    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    @Schema(description = "Applied job.")
    private Job job;

    /**
     * The current status of the application.
     * Defaults to {@link ApplicationStatus#APPLIED}.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Current application status.", example = "APPLIED")
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    /**
     * The timestamp when the application was created.
     */
    @Column(name = "created_at", nullable = false)
    @Schema(description = "Date-time when the application was created.", example = "2026-02-14T12:30:00")
    private LocalDateTime createdAt;

    /**
     * The timestamp when the application was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    @Schema(description = "Date-time when the application was last updated.", example = "2026-02-14T12:45:00")
    private LocalDateTime updatedAt;

    /**
     * Constructs a new Application with the specified user and job.
     * Sets the initial status to {@link ApplicationStatus#APPLIED}.
     *
     * @param user the applicant user
     * @param job  the job being applied for
     */
    public Application(User user, Job job) {
        this.user = user;
        this.job = job;
        this.status = ApplicationStatus.APPLIED;
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
