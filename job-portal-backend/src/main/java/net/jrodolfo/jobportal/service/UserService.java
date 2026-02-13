package net.jrodolfo.jobportal.service;

import net.jrodolfo.jobportal.model.User;
import net.jrodolfo.jobportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        System.out.println("User id: " + id);
        User user = userRepository.getReferenceById(id);
        return user;
    }
}
