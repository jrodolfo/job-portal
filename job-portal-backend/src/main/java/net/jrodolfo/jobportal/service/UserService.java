package net.jrodolfo.jobportal.service;

import net.jrodolfo.jobportal.model.User;
import net.jrodolfo.jobportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

	@Autowired
    private UserRepository userRepository;

    // Create a new User
    public User createUser(User user) {
        return userRepository.save(user); // save(T) method from JPA Repository
    }
    
    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll(); // findAll() method from JPA Repository
    }

    // Get a user by id
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

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

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }
}
