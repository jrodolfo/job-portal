package net.jrodolfo.jobportal.repository;

import net.jrodolfo.jobportal.constant.JobStatus;
import net.jrodolfo.jobportal.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByStatusOrderByCreatedAtDesc(JobStatus status);
}
