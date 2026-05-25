package net.jrodolfo.jobportal.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiSecurityBehaviorTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createJobWithoutAuthenticationShouldReturnUnauthorizedInsteadOfRedirectingToOauth() throws Exception {
        mockMvc.perform(post("/api/jobs"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Location"));
    }

    @Test
    void createJobWithApplicantCredentialsShouldReturnForbiddenInsteadOfRedirectingToOauth() throws Exception {
        mockMvc.perform(post("/api/jobs")
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Location"));
    }
}
