package net.jrodolfo.jobportal.service;

import net.jrodolfo.jobportal.constant.JobStatus;
import net.jrodolfo.jobportal.exception.ResourceException;
import net.jrodolfo.jobportal.model.Job;
import net.jrodolfo.jobportal.repository.ApplicationRepository;
import net.jrodolfo.jobportal.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Service class for managing {@link Job} entities.
 * Handles operations like creating, updating, and retrieving job listings.
 */
@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    /**
     * Creates a new job listing and sets its initial status to {@link JobStatus#OPEN}.
     *
     * @param job the {@link Job} entity to create
     * @return the saved {@link Job}
     */
    public Job createJob(Job job) {
        job.setStatus(JobStatus.OPEN);
        return jobRepository.save(job); // save(T) method from JPA Repository
    }

    /**
     * Retrieves all jobs that are currently open, ordered by their creation date in descending order.
     *
     * @return a list of open {@link Job} entities
     */
    public List<Job> getOpenJobs() {
        return jobRepository.findByStatusOrderByCreatedAtDesc(JobStatus.OPEN);
    }

    /**
     * Retrieves all job listings, regardless of their status.
     *
     * @return a list of all {@link Job} entities
     */
    public List<Job> getAllJobs() {
        return jobRepository.findAll(); // findAll() method from JPA Repository
    }

    /**
     * Retrieves a specific job listing by its ID.
     *
     * @param id the ID of the job
     * @return the {@link Job} if found
     * @throws ResourceException if the job is not found
     */
    public Job getJobById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceException("Job with id " + id + " was not found"));
    }

    /**
     * Updates the details of an existing job.
     *
     * @param id           the ID of the job to update
     * @param incomingJob the new details for the job
     * @return the updated {@link Job}
     * @throws ResourceException if the job is not found
     */
    @Transactional
    public Job updateJob(Long id, Job incomingJob) {
        Job existingJob = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceException("Job with id " + id + " was not found"));

        existingJob.setTitle(incomingJob.getTitle());
        existingJob.setDescription(incomingJob.getDescription());
        existingJob.setCompany(incomingJob.getCompany());
        if (incomingJob.getPostedDate() != null) {
            existingJob.setPostedDate(incomingJob.getPostedDate());
        }

        return jobRepository.save(existingJob);
    }

    /**
     * Updates the status of an existing job.
     *
     * @param id     the ID of the job to update
     * @param status the new {@link JobStatus}
     * @return the updated {@link Job}
     * @throws ResourceException if the job is not found
     */
    @Transactional
    public Job updateJobStatus(Long id, JobStatus status) {
        Job existingJob = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceException("Job with id " + id + " was not found"));

        existingJob.setStatus(status);
        return jobRepository.save(existingJob);
    }

    /**
     * Deletes a job listing.
     *
     * @param id the ID of the job to delete
     * @throws ResourceException       if the job is not found
     * @throws ResponseStatusException if the job has existing applications
     */
    public void deleteJob(Long id) {
        if (!jobRepository.existsById(id)) {
            throw new ResourceException("Job with id " + id + " was not found");
        }
        if (applicationRepository.existsByJob_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete job with existing applications");
        }
        jobRepository.deleteById(id);
    }
}
