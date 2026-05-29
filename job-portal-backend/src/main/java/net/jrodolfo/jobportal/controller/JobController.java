package net.jrodolfo.jobportal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import net.jrodolfo.jobportal.constant.JobStatus;
import net.jrodolfo.jobportal.exception.ErrorResponse;
import net.jrodolfo.jobportal.model.Job;
import net.jrodolfo.jobportal.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing job postings.
 * <p>
 * Provides endpoints for creating, retrieving, updating, and deleting {@link Job} entities.
 * Some operations are restricted to administrators, while others are public.
 */
@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Jobs", description = "Job CRUD operations")
public class JobController {

    /**
     * Service for job-related business logic.
     */
    @Autowired
    private JobService jobService;

    /**
     * Creates a new job posting.
     * <p>
     * Only users with ADMIN role are permitted to create jobs.
     *
     * @param job the {@link Job} entity to create, validated against constraints
     * @return a {@link ResponseEntity} containing the created {@link Job}
     */
    @PostMapping
    @Operation(summary = "Create job", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Job created"),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Job> createJob(@Valid @RequestBody Job job) {
        job = jobService.createJob(job);
        return ResponseEntity.status(HttpStatus.CREATED).body(job);
    }

    /**
     * Retrieves all jobs that are currently open.
     * <p>
     * This is a public endpoint accessible without authentication.
     *
     * @return a {@link ResponseEntity} containing a list of open {@link Job} objects
     */
    @GetMapping
    @Operation(summary = "Get all jobs", description = "Public endpoint.")
    @ApiResponse(responseCode = "200", description = "Jobs returned")
    public ResponseEntity<List<Job>> getAllJobs() {
        return ResponseEntity.ok(jobService.getOpenJobs());
    }

    /**
     * Retrieves all jobs in the system, regardless of their status.
     * <p>
     * Intended for administrative review.
     *
     * @return a {@link ResponseEntity} containing a list of all {@link Job} objects
     */
    @GetMapping("/admin")
    @Operation(summary = "Get all jobs for admin review", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponse(responseCode = "200", description = "Jobs returned")
    public ResponseEntity<List<Job>> getAllJobsForAdmin() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    /**
     * Retrieves a specific job by its ID.
     *
     * @param id the ID of the job to retrieve, must be at least 1
     * @return a {@link ResponseEntity} containing the requested {@link Job}
     */
    @Operation(summary = "Get a job by id", description = "Retrieve a job by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job returned"),
            @ApiResponse(responseCode = "404", description = "Job not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@io.swagger.v3.oas.annotations.Parameter(description = "id of the job to retrieve") @PathVariable @Min(value = 1) long id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    /**
     * Updates an existing job posting.
     *
     * @param id  the ID of the job to update, must be at least 1
     * @param job the updated {@link Job} entity
     * @return a {@link ResponseEntity} containing the updated {@link Job}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update job", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job updated"),
            @ApiResponse(responseCode = "404", description = "Job not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Job> updateJob(@PathVariable @Min(value = 1) long id, @Valid @RequestBody Job job) {
        return ResponseEntity.ok(jobService.updateJob(id, job));
    }

    /**
     * Updates the status of a specific job.
     *
     * @param id     the ID of the job to update
     * @param status the new {@link JobStatus} to set
     * @return a {@link ResponseEntity} containing the updated {@link Job}
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "Update job status", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job status updated"),
            @ApiResponse(responseCode = "404", description = "Job not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Job> updateJobStatus(@PathVariable @Min(value = 1) long id,
                                               @RequestParam JobStatus status) {
        return ResponseEntity.ok(jobService.updateJobStatus(id, status));
    }

    /**
     * Deletes a specific job.
     * <p>
     * Deletion may be prevented if there are existing applications for the job.
     *
     * @param id the ID of the job to delete
     * @return a {@link ResponseEntity} with no content upon successful deletion
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete job", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Job deleted"),
            @ApiResponse(responseCode = "409", description = "Job has existing applications", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Job not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteJob(@PathVariable @Min(value = 1) long id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }
}
