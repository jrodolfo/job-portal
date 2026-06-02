package net.jrodolfo.jobportal.service;

import net.jrodolfo.jobportal.constant.AuthProvider;
import net.jrodolfo.jobportal.constant.Role;
import net.jrodolfo.jobportal.dto.AdminUserResponse;
import net.jrodolfo.jobportal.dto.RegisterApplicantRequest;
import net.jrodolfo.jobportal.model.User;
import net.jrodolfo.jobportal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserService}.
 * Verifies the business logic for user management, including creation, retrieval, and updates.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUserShouldPersistWithRepository() {
        User user = new User();
        user.setName("user");
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.createUser(user);

        assertSame(user, result);
        verify(userRepository).save(user);
    }

    @Test
    void getAllUsersShouldReturnAllUsers() {
        User one = new User();
        User two = new User();
        List<User> users = List.of(one, two);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertSame(users, result);
    }

    @Test
    void getUserByIdShouldReturnUserWhenFound() {
        User user = new User();
        user.setId(7L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(7L);

        assertSame(user, result);
        verify(userRepository).findById(7L);
    }

    @Test
    void updateUserShouldPersistNewValues() {
        User existing = new User();
        existing.setId(1L);
        existing.setName("old");
        User incoming = new User();
        incoming.setName("new");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        User result = userService.updateUser(1L, incoming);

        assertEquals("new", result.getName());
        verify(userRepository).save(existing);
    }

    @Test
    void deleteUserShouldDeleteWhenExists() {
        when(userRepository.existsById(3L)).thenReturn(true);

        userService.deleteUser(3L);

        verify(userRepository).deleteById(3L);
    }

    @Test
    void deleteUserShouldThrowWhenMissing() {
        when(userRepository.existsById(4L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.deleteUser(4L));

        assertEquals("User not found", ex.getMessage());
        verify(userRepository, never()).deleteById(4L);
    }

    @Test
    void registerApplicantShouldForceApplicantRoleAndEncodePassword() {
        when(userRepository.findByName("Rafael Costa")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("rafael@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("applicant123")).thenReturn("encoded-password");
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(11L);
            return user;
        });

        AdminUserResponse result = userService.registerApplicant(
                new RegisterApplicantRequest("Rafael Costa", "rafael@example.com", "applicant123")
        );

        assertEquals(11L, result.id());
        assertEquals(Role.APPLICANT, result.role());
        assertEquals("Rafael Costa", result.name());
        assertEquals("rafael@example.com", result.email());
        assertEquals(true, result.enabled());
        verify(passwordEncoder).encode("applicant123");
    }

    @Test
    void updateApplicantEnabledShouldRejectAdminUsers() {
        User admin = new User("admin", "admin@local.test", "pwd", AuthProvider.LOCAL, Role.ADMIN);
        admin.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.updateApplicantEnabled(1L, false)
        );

        assertEquals(403, ex.getStatusCode().value());
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void updateApplicantEnabledShouldDisableApplicantUsers() {
        User applicant = new User("user", "user@local.test", "pwd", AuthProvider.LOCAL, Role.APPLICANT);
        applicant.setId(2L);
        applicant.setEnabled(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(applicant));
        when(userRepository.save(applicant)).thenReturn(applicant);

        AdminUserResponse result = userService.updateApplicantEnabled(2L, false);

        assertEquals(false, result.enabled());
        verify(userRepository).save(applicant);
    }

    @Test
    void registerApplicantShouldRejectDuplicateName() {
        User existing = new User("Alice", "other@example.com", "pwd", AuthProvider.LOCAL, Role.APPLICANT);
        existing.setId(7L);
        when(userRepository.findByName("Alice")).thenReturn(Optional.of(existing));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.registerApplicant(new RegisterApplicantRequest("Alice", "alice@example.com", "alice123"))
        );

        assertEquals(409, ex.getStatusCode().value());
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
    }
}
