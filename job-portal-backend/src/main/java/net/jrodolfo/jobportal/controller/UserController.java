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
import net.jrodolfo.jobportal.dto.CreateApplicantUserRequest;
import net.jrodolfo.jobportal.dto.UpdateApplicantUserRequest;
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
 * REST controller for managing users.
 * <p>
 * Provides endpoints for creating, retrieving, updating, and deleting {@link User} entities.
 * Most operations are restricted to administrators.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User CRUD operations")
public class UserController {

    /**
     * Service for user-related business logic.
     */
    @Autowired
    private UserService userService;

    /**
     * Creates a new user.
     * <p>
     * Access is typically restricted to users with the ADMIN role.
     *
     * @param user the {@link User} entity to create
     * @return a {@link ResponseEntity} containing the created {@link User}
     */
    @PostMapping
    @Operation(summary = "Create user", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<User> createUser(@RequestBody User user) {
        throw new ResponseStatusException(HttpStatus.GONE, "Use /api/users/admin/applicants for user management");
    }

    /**
     * Retrieves a list of all users.
     *
     * @return a {@link ResponseEntity} containing a list of all {@link User} objects
     */
    @GetMapping
    @Operation(summary = "Get all users", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponse(responseCode = "200", description = "Users returned")
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAdminUserList());
    }

    @GetMapping("/admin")
    @Operation(summary = "Get users for admin management", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponse(responseCode = "200", description = "Admin user list returned")
    public ResponseEntity<List<AdminUserResponse>> getAdminUsers() {
        return ResponseEntity.ok(userService.getAdminUserList());
    }

    @PostMapping("/admin/applicants")
    @Operation(summary = "Create applicant user", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Applicant user created"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Duplicate user", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AdminUserResponse> createApplicantUser(@Valid @RequestBody CreateApplicantUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createApplicantUser(request));
    }

    @PutMapping("/admin/applicants/{id}")
    @Operation(summary = "Update applicant user", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Applicant user updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not an applicant user", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Duplicate user", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AdminUserResponse> updateApplicantUser(@PathVariable @Min(value = 1) long id,
                                                                 @Valid @RequestBody UpdateApplicantUserRequest request) {
        return ResponseEntity.ok(userService.updateApplicantUser(id, request));
    }

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
     * Retrieves a specific user by their ID.
     *
     * @param id the ID of the user to retrieve, must be at least 1
     * @return a {@link ResponseEntity} containing the requested {@link User}
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
     * Updates an existing user's information.
     *
     * @param id   the ID of the user to update, must be at least 1
     * @param user the updated {@link User} entity
     * @return a {@link ResponseEntity} containing the updated {@link User}
     * @throws ResourceException if no user is found with the given ID
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update user", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<User> updateUser(@PathVariable @Min(value = 1) long id, @RequestBody User user) {
        throw new ResponseStatusException(HttpStatus.GONE, "Use /api/users/admin/applicants/{id} for user management");
    }

    /**
     * Deletes a specific user.
     *
     * @param id the ID of the user to delete, must be at least 1
     * @return a {@link ResponseEntity} with no content upon successful deletion
     * @throws ResourceException if no user is found with the given ID
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteUser(@PathVariable @Min(value = 1) long id) {
        throw new ResponseStatusException(HttpStatus.GONE, "User hard delete is not supported");
    }
}
