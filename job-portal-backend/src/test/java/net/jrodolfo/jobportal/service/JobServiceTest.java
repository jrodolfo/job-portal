package net.jrodolfo.jobportal.service;

import net.jrodolfo.jobportal.model.Job;
import net.jrodolfo.jobportal.repository.JobRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobService jobService;

    @Test
    void createJobShouldPersistWithRepository() {
        Job job = new Job("Java Dev", "Build apps", "ACME");
        when(jobRepository.save(job)).thenReturn(job);

        Job result = jobService.createJob(job);

        assertSame(job, result);
        verify(jobRepository).save(job);
    }

    @Test
    void getAllJobsShouldReturnAllJobs() {
        Job one = new Job("Java Dev", "Build apps", "ACME");
        Job two = new Job("QA", "Test apps", "ACME");
        List<Job> jobs = List.of(one, two);
        when(jobRepository.findAll()).thenReturn(jobs);

        List<Job> result = jobService.getAllJobs();

        assertEquals(2, result.size());
        assertSame(jobs, result);
    }

    @Test
    void getJobByIdShouldReturnJobWhenFound() {
        Job job = new Job("Java Dev", "Build apps", "ACME");
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        Job result = jobService.getJobById(1L);

        assertSame(job, result);
    }

    @Test
    void getJobByIdShouldThrowWhenMissing() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> jobService.getJobById(99L));

        assertEquals("Job not found", exception.getMessage());
    }
}
