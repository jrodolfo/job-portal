package net.jrodolfo.jobportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import net.jrodolfo.jobportal.model.Job;
import net.jrodolfo.jobportal.model.User;

import net.jrodolfo.jobportal.model.Application;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long>{
    boolean existsByUserAndJob(User user, Job job);
    List<Application> findByUser_Name(String username);
}
