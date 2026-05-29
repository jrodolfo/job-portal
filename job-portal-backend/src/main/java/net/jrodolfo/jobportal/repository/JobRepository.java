package net.jrodolfo.jobportal.repository;

import net.jrodolfo.jobportal.constant.JobStatus;
import net.jrodolfo.jobportal.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for {@link Job} entities.
 * Provides methods for managing job postings in the database.
 */
public interface JobRepository extends JpaRepository<Job, Long> {

    /**
     * Finds all jobs with a specific status, ordered by their creation date in descending order.
     *
     * @param status the status of the jobs to find
     * @return a list of jobs matching the status, sorted from newest to oldest
     */
    List<Job> findByStatusOrderByCreatedAtDesc(JobStatus status);
}
