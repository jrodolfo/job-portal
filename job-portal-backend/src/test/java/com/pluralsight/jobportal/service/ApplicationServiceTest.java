package com.pluralsight.jobportal.service;

import com.pluralsight.jobportal.model.Application;
import com.pluralsight.jobportal.model.Job;
import com.pluralsight.jobportal.model.User;
import com.pluralsight.jobportal.repository.ApplicationRepository;
import com.pluralsight.jobportal.repository.JobRepository;
import com.pluralsight.jobportal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private ApplicationService applicationService;

    @Test
    void applyForJobShouldSaveApplicationWhenUserAndJobExist() {
        User user = new User();
        user.setName("user");
        Job job = new Job();
        job.setId(10L);

        Application saved = new Application(user, job);
        saved.setId(99L);

        when(userRepository.findByName("user")).thenReturn(Optional.of(user));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
        when(applicationRepository.save(org.mockito.ArgumentMatchers.any(Application.class))).thenReturn(saved);

        Application result = applicationService.applyForJob("user", 10L);

        assertSame(saved, result);
        ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(captor.capture());
        assertSame(user, captor.getValue().getUser());
        assertSame(job, captor.getValue().getJob());
    }

    @Test
    void applyForJobShouldThrowWhenUserMissing() {
        when(userRepository.findByName("missing")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> applicationService.applyForJob("missing", 10L)
        );

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void applyForJobShouldThrowWhenJobMissing() {
        User user = new User();
        user.setName("user");
        when(userRepository.findByName("user")).thenReturn(Optional.of(user));
        when(jobRepository.findById(404L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> applicationService.applyForJob("user", 404L)
        );

        assertEquals("Job not found", exception.getMessage());
    }
}
