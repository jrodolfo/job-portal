package net.jrodolfo.jobportal.repository;

import net.jrodolfo.jobportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for {@link User} entities.
 * Provides methods for managing user accounts in the database.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their display name.
     *
     * @param username the name of the user to find
     * @return an {@link Optional} containing the user if found
     */
    Optional<User> findByName(String username);
}
