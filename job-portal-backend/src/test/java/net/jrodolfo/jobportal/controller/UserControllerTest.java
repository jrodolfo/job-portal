package net.jrodolfo.jobportal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.jrodolfo.jobportal.constant.Role;
import net.jrodolfo.jobportal.dto.AdminUserResponse;
import net.jrodolfo.jobportal.dto.CreateApplicantUserRequest;
import net.jrodolfo.jobportal.dto.UpdateApplicantUserRequest;
import net.jrodolfo.jobportal.dto.UpdateUserEnabledRequest;
import net.jrodolfo.jobportal.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link UserController}.
 * Verifies user management operations and ensures they are restricted to users with ADMIN role.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void adminUserManagementShouldExposeApplicantOnlyOperations() throws Exception {
        AdminUserResponse createdUser = new AdminUserResponse(10L, "Alice", "alice@example.com", null, Role.APPLICANT, true, null, null);
        AdminUserResponse updatedUser = new AdminUserResponse(10L, "Alice Smith", "alice.smith@example.com", null, Role.APPLICANT, true, null, null);
        AdminUserResponse disabledUser = new AdminUserResponse(10L, "Alice Smith", "alice.smith@example.com", null, Role.APPLICANT, false, null, null);

        when(userService.getAdminUserList()).thenReturn(java.util.List.of(createdUser));
        when(userService.getAdminUserById(10L)).thenReturn(createdUser);
        when(userService.createApplicantUser(any(CreateApplicantUserRequest.class))).thenReturn(createdUser);
        when(userService.updateApplicantUser(eq(10L), any(UpdateApplicantUserRequest.class))).thenReturn(updatedUser);
        when(userService.updateApplicantEnabled(eq(10L), eq(false))).thenReturn(disabledUser);

        mockMvc.perform(get("/api/users/admin")
                        .with(httpBasic("admin@local.test", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[0].role").value("APPLICANT"))
                .andExpect(jsonPath("$[0].enabled").value(true));

        mockMvc.perform(get("/api/users/10")
                        .with(httpBasic("admin@local.test", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(post("/api/users/admin/applicants")
                        .with(httpBasic("admin@local.test", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateApplicantUserRequest("Alice", "alice@example.com", "alice123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("APPLICANT"));

        mockMvc.perform(put("/api/users/admin/applicants/10")
                        .with(httpBasic("admin@local.test", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateApplicantUserRequest("Alice Smith", "alice.smith@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Smith"));

        mockMvc.perform(put("/api/users/admin/applicants/10/enabled")
                        .with(httpBasic("admin@local.test", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserEnabledRequest(false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));

        verify(userService).updateApplicantEnabled(10L, false);

        mockMvc.perform(get("/api/users/admin")
                        .with(httpBasic("user@local.test", "user123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void legacyUserMutationEndpointsShouldBeRetired() throws Exception {
        mockMvc.perform(post("/api/users")
                        .with(httpBasic("admin@local.test", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isGone());

        mockMvc.perform(put("/api/users/1")
                        .with(httpBasic("admin@local.test", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isGone());
    }
}
