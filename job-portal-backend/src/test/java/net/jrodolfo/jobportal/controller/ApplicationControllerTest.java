package net.jrodolfo.jobportal.controller;

import net.jrodolfo.jobportal.constant.ApplicationStatus;
import net.jrodolfo.jobportal.model.Application;
import net.jrodolfo.jobportal.model.Job;
import net.jrodolfo.jobportal.model.User;
import net.jrodolfo.jobportal.service.ApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplicationService applicationService;

    @Test
    void applyForJobShouldReturnUnauthorizedForAnonymous() throws Exception {
        mockMvc.perform(post("/api/applications/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void applyForJobShouldReturnForbiddenForAdmin() throws Exception {
        mockMvc.perform(post("/api/applications/1")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void applyForJobShouldReturnCreatedForApplicant() throws Exception {
        User user = new User();
        user.setName("user");
        Job job = new Job("Java Developer", "Build APIs", "ACME");
        job.setId(1L);
        Application application = new Application(user, job);
        application.setId(50L);

        when(applicationService.applyForJob("user", 1L)).thenReturn(application);

        mockMvc.perform(post("/api/applications/1")
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isCreated());
    }

    @Test
    void getApplicationsShouldRequireAuthentication() throws Exception {
        when(applicationService.getApplicationsByUsername("user")).thenReturn(List.of(new Application()));

        mockMvc.perform(get("/api/applications")
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void putApplicationShouldAllowApplicantWithdrawAndForbidOtherStatus() throws Exception {
        User user = new User();
        user.setName("user");
        Job job = new Job("Java Developer", "Build APIs", "ACME");
        Application application = new Application(user, job);
        application.setId(10L);

        when(applicationService.getApplicationById(10L)).thenReturn(application);
        when(applicationService.updateApplicationStatus(eq(10L), eq(ApplicationStatus.WITHDRAWN))).thenReturn(application);

        mockMvc.perform(put("/api/applications/10")
                        .with(httpBasic("user", "user123"))
                        .param("status", "WITHDRAWN"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/applications/10")
                        .with(httpBasic("user", "user123"))
                        .param("status", "ACCEPTED"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteApplicationShouldAllowOwner() throws Exception {
        User user = new User();
        user.setName("user");
        Job job = new Job("Java Developer", "Build APIs", "ACME");
        Application application = new Application(user, job);
        application.setId(11L);
        when(applicationService.getApplicationById(11L)).thenReturn(application);

        mockMvc.perform(delete("/api/applications/11")
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isNoContent());
    }
}
