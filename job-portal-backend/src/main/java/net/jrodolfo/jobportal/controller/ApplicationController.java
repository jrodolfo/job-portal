package net.jrodolfo.jobportal.controller;

import java.security.Principal;
import java.util.List;

import net.jrodolfo.jobportal.constant.ApplicationStatus;
import net.jrodolfo.jobportal.exception.ResourceException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import net.jrodolfo.jobportal.model.Application;
import net.jrodolfo.jobportal.service.ApplicationService;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    final ApplicationService applicationService;

    @Autowired
    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    // Apply for a job - must be called only by logged users
    @PostMapping("/{jobId}") // Path variable correctly mapped
    public ResponseEntity<Application> applyForJob(Principal principal, @PathVariable Long jobId) {
        String username = principal.getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.applyForJob(username, jobId));
    }

    @GetMapping
    public ResponseEntity<List<Application>> getApplications(Authentication authentication) {
        if (isAdmin(authentication)) {
            return ResponseEntity.ok(applicationService.getAllApplications());
        }
        return ResponseEntity.ok(applicationService.getApplicationsByUsername(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Application> getApplicationById(@PathVariable Long id, Authentication authentication) {
        Application application = getAuthorizedApplication(id, authentication);
        return ResponseEntity.ok(application);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Application> updateApplication(@PathVariable Long id,
                                                         @RequestParam ApplicationStatus status,
                                                         Authentication authentication) {
        Application existingApplication = getAuthorizedApplication(id, authentication);

        // Applicants can only withdraw their own applications.
        if (!isAdmin(authentication) && status != ApplicationStatus.WITHDRAWN) {
            throw new ResourceException("Applicants can only set application status to WITHDRAWN");
        }

        Application updatedApplication = applicationService.updateApplicationStatus(existingApplication.getId(), status);
        return ResponseEntity.ok(updatedApplication);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id, Authentication authentication) {
        Application application = getAuthorizedApplication(id, authentication);
        applicationService.deleteApplication(application.getId());
        return ResponseEntity.noContent().build();
    }

    private Application getAuthorizedApplication(Long id, Authentication authentication) {
        Application application;
        try {
            application = applicationService.getApplicationById(id);
        } catch (Exception e) {
            throw new ResourceException("Application with id " + id + " was not found");
        }

        if (isAdmin(authentication)) {
            return application;
        }

        if (application.getUser() == null || application.getUser().getName() == null ||
                !application.getUser().getName().equals(authentication.getName())) {
            throw new ResourceException("Application with id " + id + " was not found");
        }

        return application;
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
