package com.pluralsight.jobportal.controller;

import com.pluralsight.jobportal.model.Application;
import com.pluralsight.jobportal.model.Job;
import com.pluralsight.jobportal.model.User;
import com.pluralsight.jobportal.service.ApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
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
    void applyForJobShouldReturnOkForApplicant() throws Exception {
        User user = new User();
        user.setName("user");
        Job job = new Job("Java Developer", "Build APIs", "ACME");
        job.setId(1L);
        Application application = new Application(user, job);
        application.setId(50L);

        when(applicationService.applyForJob("user", 1L)).thenReturn(application);

        mockMvc.perform(post("/api/applications/1")
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isOk());
    }
}
