package com.pluralsight.jobportal.service;

import com.pluralsight.jobportal.model.User;
import com.pluralsight.jobportal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
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
    void getUserByIdShouldReturnReferenceFromRepository() {
        User user = new User();
        user.setId(7L);
        when(userRepository.getReferenceById(7L)).thenReturn(user);

        User result = userService.getUserById(7L);

        assertSame(user, result);
        verify(userRepository).getReferenceById(7L);
    }
}
