package net.jrodolfo.jobportal.repository;

import net.jrodolfo.jobportal.model.Application;
import net.jrodolfo.jobportal.model.Job;
import net.jrodolfo.jobportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link Application} entities.
 * Provides methods for managing job applications in the database.
 */
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /**
     * Checks if an application already exists for the given user and job.
     *
     * @param user the user to check
     * @param job  the job to check
     * @return {@code true} if an application exists, {@code false} otherwise
     */
    boolean existsByUserAndJob(User user, Job job);

    /**
     * Finds an application for the given user and job.
     *
     * @param user the user who applied
     * @param job  the job applied for
     * @return an {@link Optional} containing the application if found
     */
    Optional<Application> findByUserAndJob(User user, Job job);

    /**
     * Checks if any applications exist for a specific job ID.
     *
     * @param jobId the ID of the job
     * @return {@code true} if at least one application exists for the job, {@code false} otherwise
     */
    boolean existsByJob_Id(Long jobId);

    /**
     * Finds all applications submitted by a user with the specified email.
     *
     * @param email the email of the user
     * @return a list of applications matching the email
     */
    List<Application> findByUser_EmailIgnoreCase(String email);
}
