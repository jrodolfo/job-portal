package net.jrodolfo.jobportal.controller;

import net.jrodolfo.jobportal.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for JWT security.
 * Verifies that the security filter chain correctly handles real JWT tokens and enforces access control.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiSecurityJwtIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void adminEndpointShouldAcceptRealJwtTokenThroughSecurityChain() throws Exception {
        String token = jwtUtil.generateToken("admin@local.test");

        mockMvc.perform(get("/api/jobs/admin")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Location"));
    }

    @Test
    void adminEndpointShouldRejectApplicantTokenThroughSecurityChain() throws Exception {
        String token = jwtUtil.generateToken("user@local.test");

        mockMvc.perform(get("/api/jobs/admin")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Location"));
    }

    @Test
    void adminEndpointShouldRequireAuthenticationWhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/api/jobs/admin"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Location"));
    }
}
