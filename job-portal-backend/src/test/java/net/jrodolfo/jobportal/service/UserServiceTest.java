package net.jrodolfo.jobportal.service;

import net.jrodolfo.jobportal.model.User;
import net.jrodolfo.jobportal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

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
}
