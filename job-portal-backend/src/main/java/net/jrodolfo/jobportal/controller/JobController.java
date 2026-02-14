package net.jrodolfo.jobportal.controller;

import java.util.List;

import net.jrodolfo.jobportal.exception.ResourceException;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import net.jrodolfo.jobportal.model.Job;
import net.jrodolfo.jobportal.service.JobService;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

	@Autowired
	private JobService jobService;

	 // Create a new job, Allowed user: ADMIN
    @PostMapping
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
    public ResponseEntity<List<Job>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @Operation(summary = "Get a job by id", description = "Retrieve a job by id")
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
    public ResponseEntity<Job> updateJob(@PathVariable @Min(value = 1) long id, @RequestBody Job job) {
        try {
            return ResponseEntity.ok(jobService.updateJob(id, job));
        } catch (Exception e) {
            throw new ResourceException("Job with id " + id + " was not found");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable @Min(value = 1) long id) {
        try {
            jobService.deleteJob(id);
        } catch (Exception e) {
            throw new ResourceException("Job with id " + id + " was not found");
        }
        return ResponseEntity.noContent().build();
    }
}
