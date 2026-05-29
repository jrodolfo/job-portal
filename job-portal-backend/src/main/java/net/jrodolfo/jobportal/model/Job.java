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

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Job posting.")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique job id.", example = "10")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Job title.", example = "Java Developer")
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be at most 255 characters")
    private String title;

    @Column(nullable = false, length = 2000)
    @Schema(description = "Job description.", example = "Develop Java applications and services")
    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;

    @Column(nullable = false)
    @Schema(description = "Company name.", example = "ACME")
    @NotBlank(message = "Company is required")
    @Size(max = 255, message = "Company must be at most 255 characters")
    private String company;

    @Column(nullable = false)
    @Schema(description = "Date the job was posted.", example = "2026-02-14")
    private LocalDate postedDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Current lifecycle status for the job.", example = "OPEN")
    private JobStatus status = JobStatus.OPEN;

    @Column(name = "created_at", nullable = false)
    @Schema(description = "Date-time when the job record was created.", example = "2026-02-14T12:30:00")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @Schema(description = "Date-time when the job record was last updated.", example = "2026-02-14T12:45:00")
    private LocalDateTime updatedAt;

    public Job(String title, String description, String company) {
        this.title = title;
        this.description = description;
        this.company = company;
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
