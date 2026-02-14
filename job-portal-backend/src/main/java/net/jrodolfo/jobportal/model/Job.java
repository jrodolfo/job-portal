package net.jrodolfo.jobportal.model;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String title;

    @Column(nullable = false)
    @Schema(description = "Job description.", example = "Develop Java applications and services")
    private String description;

    @Column(nullable = false)
    @Schema(description = "Company name.", example = "ACME")
    private String company;

    @Column(nullable = false)
    @Schema(description = "Date the job was posted.", example = "2026-02-14")
    private LocalDate postedDate = LocalDate.now();

    public Job(String title, String description, String company) {
        this.title = title;
        this.description = description;
        this.company = company;
    }
}
