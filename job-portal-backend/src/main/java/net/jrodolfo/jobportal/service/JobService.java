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

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    // Create a new Job
    public Job createJob(Job job) {
        job.setStatus(JobStatus.OPEN);
        return jobRepository.save(job); // save(T) method from JPA Repository
    }

    // Get jobs visible to applicants/public
    public List<Job> getOpenJobs() {
        return jobRepository.findByStatusOrderByCreatedAtDesc(JobStatus.OPEN);
    }

    // Get all jobs for admins
    public List<Job> getAllJobs() {
        return jobRepository.findAll(); // findAll() method from JPA Repository
    }

    // Get a job by id
    public Job getJobById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceException("Job with id " + id + " was not found"));
    }

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

    @Transactional
    public Job updateJobStatus(Long id, JobStatus status) {
        Job existingJob = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceException("Job with id " + id + " was not found"));

        existingJob.setStatus(status);
        return jobRepository.save(existingJob);
    }

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
