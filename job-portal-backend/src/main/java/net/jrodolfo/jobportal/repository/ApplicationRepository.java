package net.jrodolfo.jobportal.repository;

import net.jrodolfo.jobportal.model.Application;
import net.jrodolfo.jobportal.model.Job;
import net.jrodolfo.jobportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    boolean existsByUserAndJob(User user, Job job);

    Optional<Application> findByUserAndJob(User user, Job job);

    boolean existsByJob_Id(Long jobId);

    List<Application> findByUser_Name(String username);
}
