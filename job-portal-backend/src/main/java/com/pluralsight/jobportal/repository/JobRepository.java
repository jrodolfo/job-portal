package com.pluralsight.jobportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pluralsight.jobportal.model.Job;

public interface JobRepository extends JpaRepository<Job, Long>{

	
}
