package net.jrodolfo.jobportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.jrodolfo.jobportal.model.Job;

public interface JobRepository extends JpaRepository<Job, Long>{

	
}
