package net.jrodolfo.jobportal.repository;

import net.jrodolfo.jobportal.constant.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import net.jrodolfo.jobportal.model.Job;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long>{

    List<Job> findByStatusOrderByCreatedAtDesc(JobStatus status);
}
