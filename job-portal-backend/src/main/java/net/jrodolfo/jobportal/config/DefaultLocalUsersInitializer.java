package net.jrodolfo.jobportal.config;

import net.jrodolfo.jobportal.constant.AuthProvider;
import net.jrodolfo.jobportal.constant.Role;
import net.jrodolfo.jobportal.model.User;
import net.jrodolfo.jobportal.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DefaultLocalUsersInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultLocalUsersInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        createIfMissing("admin", "admin@local.test", "admin123", Role.ADMIN);
        createIfMissing("user", "user@local.test", "user123", Role.APPLICANT);
    }

    private void createIfMissing(String name, String email, String password, Role role) {
        if (userRepository.existsByName(name)) {
            return;
        }

        User user = new User(name, email, passwordEncoder.encode(password), AuthProvider.LOCAL, role);
        user.setEnabled(true);
        userRepository.save(user);
    }
}
