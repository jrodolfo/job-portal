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

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Jobs", description = "Job CRUD operations")
public class JobController {

    @Autowired
    private JobService jobService;

    // Create a new job, Allowed user: ADMIN
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

    // Get all jobs
    @GetMapping
    @Operation(summary = "Get all jobs", description = "Public endpoint.")
    @ApiResponse(responseCode = "200", description = "Jobs returned")
    public ResponseEntity<List<Job>> getAllJobs() {
        return ResponseEntity.ok(jobService.getOpenJobs());
    }

    @GetMapping("/admin")
    @Operation(summary = "Get all jobs for admin review", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponse(responseCode = "200", description = "Jobs returned")
    public ResponseEntity<List<Job>> getAllJobsForAdmin() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

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

    @PutMapping("/{id}")
    @Operation(summary = "Update job", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job updated"),
            @ApiResponse(responseCode = "404", description = "Job not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Job> updateJob(@PathVariable @Min(value = 1) long id, @Valid @RequestBody Job job) {
        return ResponseEntity.ok(jobService.updateJob(id, job));
    }

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
