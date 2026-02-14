package net.jrodolfo.jobportal.controller;

import java.util.List;

import net.jrodolfo.jobportal.exception.ResourceException;
import net.jrodolfo.jobportal.exception.ErrorResponse;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.jrodolfo.jobportal.model.Job;
import net.jrodolfo.jobportal.service.JobService;

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
    public ResponseEntity<Job> createJob(@RequestBody Job job) {
        try {
            job = jobService.createJob(job);
        } catch (Exception e) {
            throw new ResourceException("Not able to create the job " + job.getTitle());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(job);
    }
    
    // Get all jobs
    @GetMapping
    @Operation(summary = "Get all jobs", description = "Public endpoint.")
    @ApiResponse(responseCode = "200", description = "Jobs returned")
    public ResponseEntity<List<Job>> getAllJobs() {
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
        Job job;
        try {
            job = jobService.getJobById(id);
        } catch (Exception e) {
            throw new ResourceException("Job with id " + id + " was not found");
        }
        return ResponseEntity.ok(job);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update job", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job updated"),
            @ApiResponse(responseCode = "404", description = "Job not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Job> updateJob(@PathVariable @Min(value = 1) long id, @RequestBody Job job) {
        try {
            return ResponseEntity.ok(jobService.updateJob(id, job));
        } catch (Exception e) {
            throw new ResourceException("Job with id " + id + " was not found");
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete job", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Job deleted"),
            @ApiResponse(responseCode = "404", description = "Job not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteJob(@PathVariable @Min(value = 1) long id) {
        try {
            jobService.deleteJob(id);
        } catch (Exception e) {
            throw new ResourceException("Job with id " + id + " was not found");
        }
        return ResponseEntity.noContent().build();
    }
}
