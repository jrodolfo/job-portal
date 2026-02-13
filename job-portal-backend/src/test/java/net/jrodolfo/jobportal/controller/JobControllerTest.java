package net.jrodolfo.jobportal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.jrodolfo.jobportal.model.Job;
import net.jrodolfo.jobportal.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JobService jobService;

    @Test
    void createJobShouldReturnForbiddenForApplicant() throws Exception {
        Job job = new Job("Java Developer", "Build APIs", "ACME");

        mockMvc.perform(post("/api/jobs")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(job)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createJobShouldReturnOkForAdminAndCallServiceOnce() throws Exception {
        Job request = new Job("Java Developer", "Build APIs", "ACME");
        Job response = new Job("Java Developer", "Build APIs", "ACME");
        response.setId(1L);

        when(jobService.createJob(org.mockito.ArgumentMatchers.any(Job.class))).thenReturn(response);

        mockMvc.perform(post("/api/jobs")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Java Developer"));

        verify(jobService, times(1)).createJob(org.mockito.ArgumentMatchers.any(Job.class));
    }

    @Test
    void getAllJobsShouldBePublic() throws Exception {
        when(jobService.getAllJobs()).thenReturn(List.of(
                new Job("Java Developer", "Build APIs", "ACME"),
                new Job("QA Engineer", "Test releases", "ACME")
        ));

        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk());
    }
}
