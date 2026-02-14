package net.jrodolfo.jobportal.controller;

import java.security.Principal;
import java.util.List;

import net.jrodolfo.jobportal.constant.ApplicationStatus;
import net.jrodolfo.jobportal.exception.ErrorResponse;
import net.jrodolfo.jobportal.exception.ResourceException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Applications", description = "Application CRUD operations")
public class ApplicationController {

    final ApplicationService applicationService;

    @Autowired
    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    // Apply for a job - must be called only by logged users
    @PostMapping("/{jobId}") // Path variable correctly mapped
    @Operation(summary = "Create application for a job", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Application created"),
            @ApiResponse(responseCode = "409", description = "Application already exists", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Application> applyForJob(Principal principal, @PathVariable Long jobId) {
        String username = principal.getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.applyForJob(username, jobId));
    }

    @GetMapping
    @Operation(summary = "Get applications", description = "Admins get all applications; applicants get only their own.", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponse(responseCode = "200", description = "Applications returned")
    public ResponseEntity<List<Application>> getApplications(Authentication authentication) {
        if (isAdmin(authentication)) {
            return ResponseEntity.ok(applicationService.getAllApplications());
        }
        return ResponseEntity.ok(applicationService.getApplicationsByUsername(authentication.getName()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get application by id", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Application returned"),
            @ApiResponse(responseCode = "404", description = "Application not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Application> getApplicationById(@PathVariable Long id, Authentication authentication) {
        Application application = getAuthorizedApplication(id, authentication);
        return ResponseEntity.ok(application);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update application status", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Application updated"),
            @ApiResponse(responseCode = "404", description = "Application not found or unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @Operation(summary = "Delete application", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Application deleted"),
            @ApiResponse(responseCode = "404", description = "Application not found or unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
