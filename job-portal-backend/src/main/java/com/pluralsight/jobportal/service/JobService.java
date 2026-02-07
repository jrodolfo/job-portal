package com.pluralsight.jobportal.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pluralsight.jobportal.model.Job;
import com.pluralsight.jobportal.repository.JobRepository;

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
}
