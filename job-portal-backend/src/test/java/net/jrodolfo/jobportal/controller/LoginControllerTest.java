package net.jrodolfo.jobportal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.jrodolfo.jobportal.model.User;
import net.jrodolfo.jobportal.repository.UserRepository;
import net.jrodolfo.jobportal.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link LoginController}.
 * Verifies authentication flows and retrieval of user details.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void loginShouldRequireAuthentication() throws Exception {
        mockMvc.perform(post("/api/auth/login"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }

    @Test
    void loginShouldReturnTokenForAuthenticatedUser() throws Exception {
        when(jwtUtil.generateToken("user@local.test")).thenReturn("mock-token");

        mockMvc.perform(post("/api/auth/login")
                        .with(httpBasic("user@local.test", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-token"));
    }

    @Test
    void loginShouldShowDisabledAccountMessage() throws Exception {
        User user = userRepository.findByEmailIgnoreCase("user@local.test").orElseThrow();

        try {
            user.setEnabled(false);
            userRepository.saveAndFlush(user);

            mockMvc.perform(post("/api/auth/login")
                            .with(httpBasic("user@local.test", "user123")))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Your account is disabled. Please contact an administrator."));
        } finally {
            user.setEnabled(true);
            userRepository.saveAndFlush(user);
        }
    }

    @Test
    void detailsShouldReturnAuthenticatedUserPayload() throws Exception {
        mockMvc.perform(get("/api/auth/details")
                        .with(httpBasic("user@local.test", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user@local.test"))
                .andExpect(jsonPath("$.displayName").value("user"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_APPLICANT"));
    }

    @Test
    void registerShouldCreateApplicantAccountWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Rafael Costa",
                                "email", "rafael@example.com",
                                "password", "applicant123"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Rafael Costa"))
                .andExpect(jsonPath("$.email").value("rafael@example.com"))
                .andExpect(jsonPath("$.role").value("APPLICANT"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void registerShouldIgnoreRoleEscalationAttempts() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Nadia Silva",
                                "email", "nadia@example.com",
                                "password", "applicant123",
                                "role", "ADMIN"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("APPLICANT"));
    }

    @Test
    void registerShouldRejectDuplicateEmail() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Another User",
                                "email", "user@local.test",
                                "password", "applicant123"
                        ))))
                .andExpect(status().isConflict());
    }
}
