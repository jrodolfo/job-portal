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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.jrodolfo.jobportal.constant.JobStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a job posting in the system.
 * Contains information about the job title, description, company, and its current status.
 */
@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Job posting.")
public class Job {

    /**
     * The unique identifier for the job.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique job id.", example = "10")
    private Long id;

    /**
     * The title of the job.
     */
    @Column(nullable = false)
    @Schema(description = "Job title.", example = "Java Developer")
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be at most 255 characters")
    private String title;

    /**
     * A detailed description of the job.
     */
    @Column(nullable = false, length = 2000)
    @Schema(description = "Job description.", example = "Develop Java applications and services")
    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;

    /**
     * The name of the company posting the job.
     */
    @Column(nullable = false)
    @Schema(description = "Company name.", example = "ACME")
    @NotBlank(message = "Company is required")
    @Size(max = 255, message = "Company must be at most 255 characters")
    private String company;

    /**
     * The date when the job was posted.
     * Defaults to the current date.
     */
    @Column(nullable = false)
    @Schema(description = "Date the job was posted.", example = "2026-02-14")
    private LocalDate postedDate = LocalDate.now();

    /**
     * The current status of the job posting.
     * Defaults to {@link JobStatus#OPEN}.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Current lifecycle status for the job.", example = "OPEN")
    private JobStatus status = JobStatus.OPEN;

    /**
     * The timestamp when the job record was created.
     */
    @Column(name = "created_at", nullable = false)
    @Schema(description = "Date-time when the job record was created.", example = "2026-02-14T12:30:00")
    private LocalDateTime createdAt;

    /**
     * The timestamp when the job record was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    @Schema(description = "Date-time when the job record was last updated.", example = "2026-02-14T12:45:00")
    private LocalDateTime updatedAt;

    /**
     * Constructs a new Job with the specified title, description, and company.
     *
     * @param title       the job title
     * @param description the job description
     * @param company     the company name
     */
    public Job(String title, String description, String company) {
        this.title = title;
        this.description = description;
        this.company = company;
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
