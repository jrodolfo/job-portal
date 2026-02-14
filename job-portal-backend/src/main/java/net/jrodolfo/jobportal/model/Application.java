package net.jrodolfo.jobportal.model;

import net.jrodolfo.jobportal.constant.ApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique application id.", example = "100")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "Applicant user.")
    private User user;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    @Schema(description = "Applied job.")
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Current application status.", example = "APPLIED")
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(nullable = false)
    @Schema(description = "Date-time when the application was created.", example = "2026-02-14T12:30:00")
    private LocalDateTime appliedAt;

    @Column(nullable = false)
    @Schema(description = "Date-time when the application was last updated.", example = "2026-02-14T12:45:00")
    private LocalDateTime updatedAt;

    public Application(User user, Job job) {
        this.user = user;
        this.job = job;
        this.status = ApplicationStatus.APPLIED;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (appliedAt == null) {
            appliedAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
