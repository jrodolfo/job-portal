package net.jrodolfo.jobportal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security tests for job creation endpoints.
 * Verifies that only authorized users (ADMIN) can create new job postings.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class JobCreationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void userAccountShouldGetForbiddenForJobCreation() throws Exception {
        String jobJson = """
                {
                    "title": "Java Developer",
                    "description": "Develop Java applications and systems",
                    "company": "XYZ"
                }
                """;

        mockMvc.perform(post("/api/jobs")
                        .with(httpBasic("user@local.test", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobJson))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminAccountShouldBeAbleToCreateJob() throws Exception {
        String jobJson = """
                {
                    "title": "Java Developer",
                    "description": "Develop Java applications and systems",
                    "company": "XYZ"
                }
                """;

        mockMvc.perform(post("/api/jobs")
                        .with(httpBasic("admin@local.test", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobJson))
                .andExpect(status().isCreated());
    }
}
