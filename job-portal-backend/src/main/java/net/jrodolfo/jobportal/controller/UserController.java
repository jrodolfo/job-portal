package net.jrodolfo.jobportal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
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
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(user));
    }

    /**
     * Retrieves a list of all users.
     *
     * @return a {@link ResponseEntity} containing a list of all {@link User} objects
     */
    @GetMapping
    @Operation(summary = "Get all users", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponse(responseCode = "200", description = "Users returned")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
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
    public ResponseEntity<User> getUserById(@io.swagger.v3.oas.annotations.Parameter(description = "id of the user to retrieve") @PathVariable @Min(value = 1) long id) {
        User user;
        try {
            user = userService.getUserById(id);
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
        try {
            return ResponseEntity.ok(userService.updateUser(id, user));
        } catch (Exception e) {
            throw new ResourceException("User with id " + id + " was not found");
        }
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
        try {
            userService.deleteUser(id);
        } catch (Exception e) {
            throw new ResourceException("User with id " + id + " was not found");
        }
        return ResponseEntity.noContent().build();
    }
}
