package net.jrodolfo.jobportal.service;

import net.jrodolfo.jobportal.constant.ApplicationStatus;
import net.jrodolfo.jobportal.model.Application;
import net.jrodolfo.jobportal.model.Job;
import net.jrodolfo.jobportal.model.User;
import net.jrodolfo.jobportal.repository.ApplicationRepository;
import net.jrodolfo.jobportal.repository.JobRepository;
import net.jrodolfo.jobportal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.server.ResponseStatusException;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        when(applicationRepository.existsByUserAndJob(user, job)).thenReturn(false);
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

    @Test
    void applyForJobShouldThrowConflictWhenAlreadyExists() {
        User user = new User();
        user.setName("user");
        Job job = new Job();
        job.setId(10L);

        when(userRepository.findByName("user")).thenReturn(Optional.of(user));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
        when(applicationRepository.existsByUserAndJob(user, job)).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> applicationService.applyForJob("user", 10L)
        );

        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void getAllApplicationsShouldReturnRepositoryData() {
        Application one = new Application();
        Application two = new Application();
        when(applicationRepository.findAll()).thenReturn(List.of(one, two));

        List<Application> applications = applicationService.getAllApplications();

        assertEquals(2, applications.size());
    }

    @Test
    void getApplicationsByUsernameShouldReturnRepositoryData() {
        when(applicationRepository.findByUser_Name("user")).thenReturn(List.of(new Application()));

        List<Application> applications = applicationService.getApplicationsByUsername("user");

        assertEquals(1, applications.size());
    }

    @Test
    void updateApplicationStatusShouldPersistStatus() {
        Application application = new Application();
        application.setId(50L);
        when(applicationRepository.findById(50L)).thenReturn(Optional.of(application));
        when(applicationRepository.save(application)).thenReturn(application);

        Application result = applicationService.updateApplicationStatus(50L, ApplicationStatus.WITHDRAWN);

        assertEquals(ApplicationStatus.WITHDRAWN, result.getStatus());
    }

    @Test
    void deleteApplicationShouldDeleteWhenExists() {
        when(applicationRepository.existsById(55L)).thenReturn(true);

        applicationService.deleteApplication(55L);

        verify(applicationRepository).deleteById(55L);
    }

    @Test
    void getApplicationByIdShouldThrowWhenMissing() {
        when(applicationRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> applicationService.getApplicationById(999L));

        assertTrue(ex.getMessage().contains("Application not found"));
    }
}
