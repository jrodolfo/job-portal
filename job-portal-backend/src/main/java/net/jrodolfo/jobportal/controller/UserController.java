package net.jrodolfo.jobportal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.Valid;
import net.jrodolfo.jobportal.dto.AdminUserResponse;
import net.jrodolfo.jobportal.dto.UpdateUserEnabledRequest;
import net.jrodolfo.jobportal.exception.ErrorResponse;
import net.jrodolfo.jobportal.exception.ResourceException;
import net.jrodolfo.jobportal.model.User;
import net.jrodolfo.jobportal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * REST controller for user administration.
 * <p>
 * The current admin workflow is intentionally narrow: admins can list users and
 * enable or disable applicant accounts. Generic create, identity update, and
 * hard-delete endpoints remain present for API compatibility but return
 * {@code 410 Gone}.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Admin user visibility and applicant status management")
public class UserController {

    /**
     * Service for user-related business logic.
     */
    @Autowired
    private UserService userService;

    /**
     * Rejects legacy direct user creation.
     * <p>
     * Applicant accounts are created through the public registration endpoint.
     * Admin-created users, role promotion, and direct admin creation are not
     * supported.
     *
     * @param user ignored legacy request body
     * @return never returns normally because this endpoint is disabled
     */
    @PostMapping
    @Operation(summary = "Legacy direct user creation is disabled", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "410", description = "Applicant users must register through /api/auth/register"),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<User> createUser(@RequestBody User user) {
        throw new ResponseStatusException(HttpStatus.GONE, "Applicant users must register through /api/auth/register");
    }

    /**
     * Retrieves users in the admin-safe response shape.
     *
     * @return a {@link ResponseEntity} containing all users without password data
     */
    @GetMapping
    @Operation(summary = "Get users for admin management", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponse(responseCode = "200", description = "Users returned")
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAdminUserList());
    }

    /**
     * Retrieves users for the Admin dashboard Users tab.
     *
     * @return a {@link ResponseEntity} containing all users without password data
     */
    @GetMapping("/admin")
    @Operation(summary = "Get users for admin management", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponse(responseCode = "200", description = "Admin user list returned")
    public ResponseEntity<List<AdminUserResponse>> getAdminUsers() {
        return ResponseEntity.ok(userService.getAdminUserList());
    }

    /**
     * Enables or disables an applicant user.
     * <p>
     * Admin accounts cannot be modified through this endpoint. Disabling an
     * applicant prevents future login and invalidates active sessions because
     * JWT requests reload the enabled flag from the database.
     *
     * @param id      the applicant user ID, must be at least 1
     * @param request the requested enabled state
     * @return the updated admin-safe user response
     */
    @PutMapping("/admin/applicants/{id}/enabled")
    @Operation(summary = "Enable or disable applicant user", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Applicant enabled state updated"),
            @ApiResponse(responseCode = "403", description = "Not an applicant user", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AdminUserResponse> updateApplicantEnabled(@PathVariable @Min(value = 1) long id,
                                                                    @Valid @RequestBody UpdateUserEnabledRequest request) {
        return ResponseEntity.ok(userService.updateApplicantEnabled(id, request.enabled()));
    }

    /**
     * Retrieves a specific user by ID in the admin-safe response shape.
     *
     * @param id the ID of the user to retrieve, must be at least 1
     * @return a {@link ResponseEntity} containing the requested user without password data
     * @throws ResourceException if no user is found with the given ID
     */
    @Operation(summary = "Get a user by id", description = "Retrieve a user by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User returned"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}")
    @SecurityRequirement(name = "basicAuth")
    public ResponseEntity<AdminUserResponse> getUserById(@io.swagger.v3.oas.annotations.Parameter(description = "id of the user to retrieve") @PathVariable @Min(value = 1) long id) {
        AdminUserResponse user;
        try {
            user = userService.getAdminUserById(id);
        } catch (Exception e) {
            throw new ResourceException("User with id " + id + " was not found");
        }
        return ResponseEntity.ok(user);
    }

    /**
     * Rejects legacy direct user identity updates.
     * <p>
     * Admins can only enable or disable applicant users. Name, email, password,
     * role, and provider edits are intentionally unsupported.
     *
     * @param id   ignored legacy user ID
     * @param user ignored legacy request body
     * @return never returns normally because this endpoint is disabled
     */
    @PutMapping("/{id}")
    @Operation(summary = "Legacy direct user update is disabled", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "410", description = "Admin user identity edits are not supported"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<User> updateUser(@PathVariable @Min(value = 1) long id, @RequestBody User user) {
        throw new ResponseStatusException(HttpStatus.GONE, "Admin user identity edits are not supported");
    }

    /**
     * Rejects legacy user hard deletion.
     * <p>
     * Users are disabled instead of deleted so application history remains
     * intact.
     *
     * @param id ignored legacy user ID
     * @return never returns normally because this endpoint is disabled
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Legacy user hard delete is disabled", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "410", description = "User hard delete is not supported"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteUser(@PathVariable @Min(value = 1) long id) {
        throw new ResponseStatusException(HttpStatus.GONE, "User hard delete is not supported");
    }
}
