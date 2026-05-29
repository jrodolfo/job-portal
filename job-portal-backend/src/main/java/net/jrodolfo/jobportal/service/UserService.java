package net.jrodolfo.jobportal.service;

import net.jrodolfo.jobportal.model.User;
import net.jrodolfo.jobportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class for managing {@link User} entities.
 * Handles user creation, updates, and retrieval.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

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
}
