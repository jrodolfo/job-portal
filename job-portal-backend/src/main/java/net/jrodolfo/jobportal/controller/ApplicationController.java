package net.jrodolfo.jobportal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.jrodolfo.jobportal.constant.ApplicationStatus;
import net.jrodolfo.jobportal.exception.ErrorResponse;
import net.jrodolfo.jobportal.model.Application;
import net.jrodolfo.jobportal.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

/**
 * REST controller for managing job applications.
 * <p>
 * Provides endpoints for creating, retrieving, updating, and deleting {@link Application} entities.
 * Access is restricted based on the user's role (Applicant or Admin).
 */
@RestController
@RequestMapping("/api/applications")
@Tag(name = "Applications", description = "Application CRUD operations")
public class ApplicationController {

    /**
     * Service for application-related business logic.
     */
    final ApplicationService applicationService;

    /**
     * Constructs an {@code ApplicationController} with the specified {@link ApplicationService}.
     *
     * @param applicationService the service to be used for application operations
     */
    @Autowired
    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * Creates a new application for a specific job.
     * <p>
     * This endpoint must be called only by authenticated users.
     *
     * @param principal the authenticated user making the request
     * @param jobId     the ID of the job to apply for
     * @return a {@link ResponseEntity} containing the created {@link Application}
     */
    @PostMapping("/{jobId}") // Path variable correctly mapped
    @Operation(summary = "Create application for a job", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Application created"),
            @ApiResponse(responseCode = "409", description = "Application already exists", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Application> applyForJob(Principal principal, @PathVariable Long jobId) {
        String email = principal.getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.applyForJob(email, jobId));
    }

    /**
     * Retrieves a list of applications.
     * <p>
     * Admins receive all applications in the system, while applicants receive only their own applications.
     *
     * @param authentication the authentication object containing user roles and details
     * @return a {@link ResponseEntity} containing a list of {@link Application} objects
     */
    @GetMapping
    @Operation(summary = "Get applications", description = "Admins get all applications; applicants get only their own.", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponse(responseCode = "200", description = "Applications returned")
    public ResponseEntity<List<Application>> getApplications(Authentication authentication) {
        if (isAdmin(authentication)) {
            return ResponseEntity.ok(applicationService.getAllApplications());
        }
        return ResponseEntity.ok(applicationService.getApplicationsByEmail(authentication.getName()));
    }

    /**
     * Retrieves a specific application by its ID.
     * <p>
     * Access is restricted: admins can access any application, but applicants can only access their own.
     *
     * @param id             the ID of the application to retrieve
     * @param authentication the authentication object for authorization check
     * @return a {@link ResponseEntity} containing the requested {@link Application}
     * @throws ResponseStatusException if the application is not found or the user is not authorized
     */
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

    /**
     * Updates the status of an existing application.
     * <p>
     * Applicants are only permitted to change the status of their own applications to {@link ApplicationStatus#WITHDRAWN}.
     * Admins have full authority to update the status to any valid {@link ApplicationStatus}.
     *
     * @param id             the ID of the application to update
     * @param status         the new {@link ApplicationStatus} to set
     * @param authentication the authentication object for authorization check
     * @return a {@link ResponseEntity} containing the updated {@link Application}
     * @throws ResponseStatusException if the application is not found, or if an applicant tries to set a status other than WITHDRAWN
     */
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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Applicants can only set application status to WITHDRAWN");
        }

        Application updatedApplication = applicationService.updateApplicationStatus(existingApplication.getId(), status);
        return ResponseEntity.ok(updatedApplication);
    }

    /**
     * Deletes a specific application.
     * <p>
     * Access is restricted: admins can delete any application, but applicants can only delete their own.
     *
     * @param id             the ID of the application to delete
     * @param authentication the authentication object for authorization check
     * @return a {@link ResponseEntity} with no content upon successful deletion
     * @throws ResponseStatusException if the application is not found or the user is not authorized
     */
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
        Application application = applicationService.getApplicationById(id);

        if (isAdmin(authentication)) {
            return application;
        }

        if (application.getUser() == null || application.getUser().getEmail() == null ||
                !application.getUser().getEmail().equalsIgnoreCase(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to access this application");
        }

        return application;
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
