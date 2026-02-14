package net.jrodolfo.jobportal.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.jrodolfo.jobportal.model.Job;
import net.jrodolfo.jobportal.repository.JobRepository;

@Service
public class JobService {

	@Autowired
    private JobRepository jobRepository;

    // Create a new Job
    public Job createJob(Job job) {
        return jobRepository.save(job); // save(T) method from JPA Repository
    }
    
    // Get all jobs
    public List<Job> getAllJobs() {
        return jobRepository.findAll(); // findAll() method from JPA Repository
    }

    // Get a job by id
    public Job getJobById(Long id) {
        return jobRepository.findById(id).orElseThrow(() -> new RuntimeException("Job not found")); // findById(id) method from JPA Repository
    }

    @Transactional
    public Job updateJob(Long id, Job incomingJob) {
        Job existingJob = jobRepository.findById(id).orElseThrow(() -> new RuntimeException("Job not found"));

        existingJob.setTitle(incomingJob.getTitle());
        existingJob.setDescription(incomingJob.getDescription());
        existingJob.setCompany(incomingJob.getCompany());
        if (incomingJob.getPostedDate() != null) {
            existingJob.setPostedDate(incomingJob.getPostedDate());
        }

        return jobRepository.save(existingJob);
    }

    public void deleteJob(Long id) {
        if (!jobRepository.existsById(id)) {
            throw new RuntimeException("Job not found");
        }
        jobRepository.deleteById(id);
    }
}
