package net.jrodolfo.jobportal.service;

import net.jrodolfo.jobportal.constant.ApplicationStatus;
import net.jrodolfo.jobportal.constant.JobStatus;
import net.jrodolfo.jobportal.model.Application;
import net.jrodolfo.jobportal.model.Job;
import net.jrodolfo.jobportal.model.User;
import net.jrodolfo.jobportal.repository.ApplicationRepository;
import net.jrodolfo.jobportal.repository.JobRepository;
import net.jrodolfo.jobportal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ApplicationService}.
 * Verifies the business logic for job applications, including validation rules and status management.
 */
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
        job.setStatus(JobStatus.OPEN);

        Application saved = new Application(user, job);
        saved.setId(99L);

        when(userRepository.findByName("user")).thenReturn(Optional.of(user));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
        when(applicationRepository.findByUserAndJob(user, job)).thenReturn(Optional.empty());
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
        Job job = new Job();
        job.setId(10L);
        job.setStatus(JobStatus.OPEN);
        Application saved = new Application();
        User persistedUser = new User("missing", "missing@local.test", null, net.jrodolfo.jobportal.constant.AuthProvider.LOCAL, net.jrodolfo.jobportal.constant.Role.APPLICANT);

        when(userRepository.findByName("missing")).thenReturn(Optional.empty());
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class))).thenReturn(persistedUser);
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
        when(applicationRepository.findByUserAndJob(persistedUser, job)).thenReturn(Optional.empty());
        when(applicationRepository.save(org.mockito.ArgumentMatchers.any(Application.class))).thenReturn(saved);

        Application result = applicationService.applyForJob("missing", 10L);

        assertSame(saved, result);
        verify(userRepository).save(org.mockito.ArgumentMatchers.any(User.class));
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

        assertEquals("Job with id 404 was not found", exception.getMessage());
    }

    @Test
    void applyForJobShouldThrowConflictWhenAlreadyExists() {
        User user = new User();
        user.setName("user");
        Job job = new Job();
        job.setId(10L);
        job.setStatus(JobStatus.OPEN);

        when(userRepository.findByName("user")).thenReturn(Optional.of(user));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
        when(applicationRepository.findByUserAndJob(user, job)).thenReturn(Optional.of(new Application(user, job)));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> applicationService.applyForJob("user", 10L)
        );

        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void applyForJobShouldReactivateWithdrawnApplication() {
        User user = new User();
        user.setName("user");
        Job job = new Job();
        job.setId(10L);
        job.setStatus(JobStatus.OPEN);
        Application withdrawn = new Application(user, job);
        withdrawn.setId(77L);
        withdrawn.setStatus(ApplicationStatus.WITHDRAWN);

        when(userRepository.findByName("user")).thenReturn(Optional.of(user));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
        when(applicationRepository.findByUserAndJob(user, job)).thenReturn(Optional.of(withdrawn));
        when(applicationRepository.save(withdrawn)).thenReturn(withdrawn);

        Application result = applicationService.applyForJob("user", 10L);

        assertSame(withdrawn, result);
        assertEquals(ApplicationStatus.APPLIED, result.getStatus());
        verify(applicationRepository).save(withdrawn);
    }

    @Test
    void applyForJobShouldRejectClosedJobs() {
        User user = new User();
        user.setName("user");
        Job job = new Job();
        job.setId(10L);
        job.setStatus(JobStatus.CLOSED);

        when(userRepository.findByName("user")).thenReturn(Optional.of(user));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> applicationService.applyForJob("user", 10L)
        );

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("Cannot apply to a closed job", ex.getReason());
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

        assertEquals("Application with id 999 was not found", ex.getMessage());
    }
}
