package net.jrodolfo.jobportal.service;

import net.jrodolfo.jobportal.constant.AuthProvider;
import net.jrodolfo.jobportal.constant.Role;
import net.jrodolfo.jobportal.dto.AdminUserResponse;
import net.jrodolfo.jobportal.dto.RegisterApplicantRequest;
import net.jrodolfo.jobportal.exception.ResourceException;
import net.jrodolfo.jobportal.model.User;
import net.jrodolfo.jobportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Service class for managing {@link User} entities.
 * Handles legacy user persistence helpers, public applicant registration, and
 * the intentionally narrow admin workflow for enabling or disabling applicants.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Creates a new user in the system.
     *
     * @param user the {@link User} entity to create
     * @return the saved {@link User}
     */
    public User createUser(User user) {
        return userRepository.save(user); // save(T) method from JPA Repository
    }

    /**
     * Retrieves all users registered in the system.
     *
     * @return a list of all {@link User} entities
     */
    public List<User> getAllUsers() {
        return userRepository.findAll(); // findAll() method from JPA Repository
    }

    /**
     * Retrieves all users in the admin-safe response shape.
     *
     * @return all users without password data
     */
    public List<AdminUserResponse> getAdminUserList() {
        return userRepository.findAll()
                .stream()
                .map(AdminUserResponse::from)
                .toList();
    }

    /**
     * Retrieves a single user in the admin-safe response shape.
     *
     * @param id the user ID
     * @return the requested user without password data
     */
    public AdminUserResponse getAdminUserById(Long id) {
        return AdminUserResponse.from(getUserById(id));
    }

    /**
     * Retrieves a specific user by their ID.
     *
     * @param id the ID of the user
     * @return the {@link User} if found
     * @throws RuntimeException if the user is not found
     */
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Updates the details of an existing user.
     *
     * @param id           the ID of the user to update
     * @param incomingUser the new details for the user
     * @return the updated {@link User}
     * @throws RuntimeException if the user is not found
     */
    @Transactional
    public User updateUser(Long id, User incomingUser) {
        User existingUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        existingUser.setName(incomingUser.getName());
        existingUser.setEmail(incomingUser.getEmail());
        existingUser.setPassword(incomingUser.getPassword());
        existingUser.setAuthProvider(incomingUser.getAuthProvider());
        existingUser.setRole(incomingUser.getRole());

        return userRepository.save(existingUser);
    }

    /**
     * Deletes a user from the system.
     *
     * @param id the ID of the user to delete
     * @throws RuntimeException if the user is not found
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    /**
     * Registers a self-service applicant account.
     * <p>
     * The account is always local-auth, applicant role, and enabled by default.
     * User names must be unique exactly as stored; emails are matched
     * case-insensitively.
     *
     * @param request the applicant registration payload
     * @return the created user without password data
     */
    @Transactional
    public AdminUserResponse registerApplicant(RegisterApplicantRequest request) {
        ensureNameAvailable(request.name(), null);
        ensureEmailAvailable(request.email(), null);

        User user = new User(
                request.name().trim(),
                request.email().trim(),
                passwordEncoder.encode(request.password()),
                AuthProvider.LOCAL,
                Role.APPLICANT
        );
        user.setEnabled(true);

        return AdminUserResponse.from(userRepository.save(user));
    }

    /**
     * Enables or disables an applicant account.
     * <p>
     * Admin users cannot be modified here. Disabling an applicant prevents
     * future login and invalidates active JWT sessions because authenticated
     * requests reload the enabled flag.
     *
     * @param id      the applicant user ID
     * @param enabled the requested enabled state
     * @return the updated user without password data
     */
    @Transactional
    public AdminUserResponse updateApplicantEnabled(Long id, boolean enabled) {
        User user = getManagedApplicant(id);
        user.setEnabled(enabled);
        return AdminUserResponse.from(userRepository.save(user));
    }

    private User getManagedApplicant(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceException("User with id " + id + " was not found"));

        if (user.getRole() != Role.APPLICANT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admins can only manage applicant users");
        }

        return user;
    }

    private void ensureNameAvailable(String name, Long currentUserId) {
        String normalizedName = name.trim();
        userRepository.findByName(normalizedName)
                .filter(existing -> !existing.getId().equals(currentUserId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "User name already exists");
                });
    }

    private void ensureEmailAvailable(String email, Long currentUserId) {
        String normalizedEmail = email.trim();
        userRepository.findByEmailIgnoreCase(normalizedEmail)
                .filter(existing -> !existing.getId().equals(currentUserId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "User email already exists");
                });
    }
}
