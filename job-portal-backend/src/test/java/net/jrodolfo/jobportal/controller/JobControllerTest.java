package net.jrodolfo.jobportal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.jrodolfo.jobportal.constant.JobStatus;
import net.jrodolfo.jobportal.exception.ResourceException;
import net.jrodolfo.jobportal.model.Job;
import net.jrodolfo.jobportal.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link JobController}.
 * Verifies job management operations including creation, updates, and status changes,
 * along with role-based access control.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JobService jobService;

    @Test
    void createJobShouldReturnForbiddenForApplicant() throws Exception {
        Job job = new Job("Java Developer", "Build APIs", "ACME");

        mockMvc.perform(post("/api/jobs")
                        .with(httpBasic("user@local.test", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(job)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createJobShouldReturnCreatedForAdminAndCallServiceOnce() throws Exception {
        Job request = new Job("Java Developer", "Build APIs", "ACME");
        Job response = new Job("Java Developer", "Build APIs", "ACME");
        response.setId(1L);

        when(jobService.createJob(any(Job.class))).thenReturn(response);

        mockMvc.perform(post("/api/jobs")
                        .with(httpBasic("admin@local.test", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Java Developer"));

        verify(jobService, times(1)).createJob(any(Job.class));
    }

    @Test
    void getAllJobsShouldBePublic() throws Exception {
        Job openJob = new Job("Java Developer", "Build APIs", "ACME");
        openJob.setStatus(JobStatus.OPEN);
        Job openJobTwo = new Job("QA Engineer", "Test releases", "ACME");
        openJobTwo.setStatus(JobStatus.OPEN);

        when(jobService.getOpenJobs()).thenReturn(List.of(
                openJob,
                openJobTwo
        ));

        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllJobsForAdminShouldRequireAdmin() throws Exception {
        Job openJob = new Job("Java Developer", "Build APIs", "ACME");
        openJob.setId(1L);
        openJob.setStatus(JobStatus.OPEN);
        Job closedJob = new Job("QA Engineer", "Test releases", "ACME");
        closedJob.setId(2L);
        closedJob.setStatus(JobStatus.CLOSED);

        when(jobService.getAllJobs()).thenReturn(List.of(openJob, closedJob));

        mockMvc.perform(get("/api/jobs/admin")
                        .with(httpBasic("admin@local.test", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("OPEN"))
                .andExpect(jsonPath("$[1].status").value("CLOSED"));

        mockMvc.perform(get("/api/jobs/admin")
                        .with(httpBasic("user@local.test", "user123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getJobByIdShouldReturnNotFoundWhenJobDoesNotExist() throws Exception {
        when(jobService.getJobById(99L)).thenThrow(new ResourceException("Job with id 99 was not found"));

        mockMvc.perform(get("/api/jobs/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Job with id 99 was not found"));
    }

    @Test
    void updateJobShouldRequireAdmin() throws Exception {
        Job request = new Job("Updated Title", "Updated Desc", "Updated Co");
        Job response = new Job("Updated Title", "Updated Desc", "Updated Co");
        response.setId(1L);
        response.setStatus(JobStatus.OPEN);
        when(jobService.updateJob(eq(1L), any(Job.class))).thenReturn(response);

        mockMvc.perform(put("/api/jobs/1")
                        .with(httpBasic("admin@local.test", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/jobs/1")
                        .with(httpBasic("user@local.test", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateJobShouldReturnNotFoundWhenJobDoesNotExist() throws Exception {
        Job request = new Job("Updated Title", "Updated Desc", "Updated Co");
        when(jobService.updateJob(eq(99L), any(Job.class)))
                .thenThrow(new ResourceException("Job with id 99 was not found"));

        mockMvc.perform(put("/api/jobs/99")
                        .with(httpBasic("admin@local.test", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Job with id 99 was not found"));
    }

    @Test
    void updateJobShouldReturnServerErrorWhenNonNotFoundFailureOccurs() throws Exception {
        Job request = new Job("Updated Title", "Updated Desc", "Updated Co");
        when(jobService.updateJob(eq(1L), any(Job.class)))
                .thenThrow(new RuntimeException("Database write failed"));

        mockMvc.perform(put("/api/jobs/1")
                        .with(httpBasic("admin@local.test", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Unexpected server error"));
    }

    @Test
    void createJobShouldReturnBadRequestWhenTitleIsBlank() throws Exception {
        Job request = new Job("  ", "Build APIs", "ACME");

        mockMvc.perform(post("/api/jobs")
                        .with(httpBasic("admin@local.test", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Title is required"));

        verifyNoInteractions(jobService);
    }

    @Test
    void createJobShouldReturnBadRequestWhenCompanyIsBlank() throws Exception {
        Job request = new Job("Java Developer", "Build APIs", " ");

        mockMvc.perform(post("/api/jobs")
                        .with(httpBasic("admin@local.test", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Company is required"));

        verifyNoInteractions(jobService);
    }

    @Test
    void createJobShouldReturnBadRequestWhenDescriptionIsBlank() throws Exception {
        Job request = new Job("Java Developer", "   ", "ACME");

        mockMvc.perform(post("/api/jobs")
                        .with(httpBasic("admin@local.test", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Description is required"));

        verifyNoInteractions(jobService);
    }

    @Test
    void createJobShouldReturnBadRequestWhenDescriptionIsTooLong() throws Exception {
        Job request = new Job("Java Developer", "x".repeat(2001), "ACME");

        mockMvc.perform(post("/api/jobs")
                        .with(httpBasic("admin@local.test", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Description must be at most 2000 characters"));

        verifyNoInteractions(jobService);
    }

    @Test
    void deleteJobShouldRequireAdmin() throws Exception {
        mockMvc.perform(delete("/api/jobs/1")
                        .with(httpBasic("admin@local.test", "admin123")))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/jobs/1")
                        .with(httpBasic("user@local.test", "user123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteJobShouldReturnNotFoundWhenJobDoesNotExist() throws Exception {
        doThrow(new ResourceException("Job with id 99 was not found")).when(jobService).deleteJob(99L);

        mockMvc.perform(delete("/api/jobs/99")
                        .with(httpBasic("admin@local.test", "admin123")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Job with id 99 was not found"));
    }

    @Test
    void deleteJobShouldReturnConflictWhenJobHasApplications() throws Exception {
        doThrow(new ResponseStatusException(CONFLICT, "Cannot delete job with existing applications"))
                .when(jobService).deleteJob(1L);

        mockMvc.perform(delete("/api/jobs/1")
                        .with(httpBasic("admin@local.test", "admin123")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Cannot delete job with existing applications"));
    }

    @Test
    void updateJobStatusShouldRequireAdmin() throws Exception {
        Job response = new Job("Java Developer", "Build APIs", "ACME");
        response.setId(1L);
        response.setStatus(JobStatus.CLOSED);
        when(jobService.updateJobStatus(1L, JobStatus.CLOSED)).thenReturn(response);

        mockMvc.perform(put("/api/jobs/1/status")
                        .with(httpBasic("admin@local.test", "admin123"))
                        .param("status", "CLOSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));

        mockMvc.perform(put("/api/jobs/1/status")
                        .with(httpBasic("user@local.test", "user123"))
                        .param("status", "CLOSED"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateJobStatusShouldAllowReopenForAdmin() throws Exception {
        Job response = new Job("Java Developer", "Build APIs", "ACME");
        response.setId(1L);
        response.setStatus(JobStatus.OPEN);
        when(jobService.updateJobStatus(1L, JobStatus.OPEN)).thenReturn(response);

        mockMvc.perform(put("/api/jobs/1/status")
                        .with(httpBasic("admin@local.test", "admin123"))
                        .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void updateJobStatusShouldReturnNotFoundWhenJobDoesNotExist() throws Exception {
        when(jobService.updateJobStatus(99L, JobStatus.CLOSED))
                .thenThrow(new ResourceException("Job with id 99 was not found"));

        mockMvc.perform(put("/api/jobs/99/status")
                        .with(httpBasic("admin@local.test", "admin123"))
                        .param("status", "CLOSED"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Job with id 99 was not found"));
    }

    @Test
    void updateJobStatusShouldReturnBadRequestForInvalidStatus() throws Exception {
        mockMvc.perform(put("/api/jobs/1/status")
                        .with(httpBasic("admin@local.test", "admin123"))
                        .param("status", "NOT_A_REAL_STATUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request"));
    }
}
