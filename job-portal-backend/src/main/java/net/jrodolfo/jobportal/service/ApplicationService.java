package net.jrodolfo.jobportal.service;

import net.jrodolfo.jobportal.constant.AuthProvider;
import net.jrodolfo.jobportal.constant.ApplicationStatus;
import net.jrodolfo.jobportal.constant.JobStatus;
import net.jrodolfo.jobportal.constant.Role;
import net.jrodolfo.jobportal.exception.ResourceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.jrodolfo.jobportal.model.Application;
import net.jrodolfo.jobportal.model.Job;
import net.jrodolfo.jobportal.model.User;
import net.jrodolfo.jobportal.repository.ApplicationRepository;
import net.jrodolfo.jobportal.repository.JobRepository;
import net.jrodolfo.jobportal.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ApplicationService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    @Autowired
    public ApplicationService(UserRepository userRepository,
                              JobRepository jobRepository,
                              ApplicationRepository applicationRepository) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    public Application applyForJob(String username, Long jobId) {
        User user = resolveApplicantUser(username);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceException("Job with id " + jobId + " was not found"));

        if (job.getStatus() == JobStatus.CLOSED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot apply to a closed job");
        }

        Application existingApplication = applicationRepository.findByUserAndJob(user, job).orElse(null);
        if (existingApplication != null) {
            if (existingApplication.getStatus() == ApplicationStatus.WITHDRAWN) {
                existingApplication.setStatus(ApplicationStatus.APPLIED);
                return applicationRepository.save(existingApplication);
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Application already exists for this user and job");
        }

        Application application = new Application(user, job);
        return applicationRepository.save(application);
    }

    private User resolveApplicantUser(String username) {
        return userRepository.findByName(username)
                .orElseGet(() -> userRepository.save(createLocalApplicantUser(username)));
    }

    private User createLocalApplicantUser(String username) {
        String email = username.contains("@") ? username : username + "@local.test";
        return new User(username, email, null, AuthProvider.LOCAL, Role.APPLICANT);
    }

    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    public List<Application> getApplicationsByUsername(String username) {
        return applicationRepository.findByUser_Name(username);
    }

    public Application getApplicationById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceException("Application with id " + id + " was not found"));
    }

    public Application updateApplicationStatus(Long id, ApplicationStatus status) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceException("Application with id " + id + " was not found"));
        application.setStatus(status);
        return applicationRepository.save(application);
    }

    public void deleteApplication(Long id) {
        if (!applicationRepository.existsById(id)) {
            throw new ResourceException("Application with id " + id + " was not found");
        }
        applicationRepository.deleteById(id);
    }
}
