package net.jrodolfo.jobportal.controller;

import net.jrodolfo.jobportal.exception.ResourceException;
import net.jrodolfo.jobportal.model.User;
import net.jrodolfo.jobportal.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Create a new user, Allowed user: ADMIN
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(user));
    }

    // Get all users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "Get a user by id", description = "Retrieve a user by id")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@io.swagger.v3.oas.annotations.Parameter(description = "id of the user to retrieve") @PathVariable @Min(value = 1) long id) {
        User user;
        try {
            user = userService.getUserById(id);
        } catch (Exception e) {
            throw new ResourceException("User with id " + id + " was not found");
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable @Min(value = 1) long id, @RequestBody User user) {
        try {
            return ResponseEntity.ok(userService.updateUser(id, user));
        } catch (Exception e) {
            throw new ResourceException("User with id " + id + " was not found");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable @Min(value = 1) long id) {
        try {
            userService.deleteUser(id);
        } catch (Exception e) {
            throw new ResourceException("User with id " + id + " was not found");
        }
        return ResponseEntity.noContent().build();
    }
}
