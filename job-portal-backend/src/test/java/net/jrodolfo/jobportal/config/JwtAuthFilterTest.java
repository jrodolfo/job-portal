package net.jrodolfo.jobportal.config;

import net.jrodolfo.jobportal.util.JwtUtil;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldLeaveContextUnauthenticatedWhenAuthorizationHeaderMissing() throws Exception {
        JwtAuthFilter filter = new JwtAuthFilter(jwtUtil, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> { };

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldAuthenticateWhenTokenIsValidAndUserExists() throws Exception {
        JwtAuthFilter filter = new JwtAuthFilter(jwtUtil, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer test-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> { };

        when(jwtUtil.extractEmail("test-token")).thenReturn("user@example.com");
        when(userDetailsService.loadUserByUsername("user@example.com"))
                .thenReturn(User.withUsername("user@example.com").password("pwd").authorities("ROLE_APPLICANT").build());
        when(jwtUtil.validateToken("test-token", "user@example.com")).thenReturn(true);

        filter.doFilter(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user@example.com", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void shouldAuthenticateGoogleUserWhenNotFoundInUserDetailsServiceButTokenValid() throws Exception {
        JwtAuthFilter filter = new JwtAuthFilter(jwtUtil, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer google-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> { };

        when(jwtUtil.extractEmail("google-token")).thenReturn("google@example.com");
        when(userDetailsService.loadUserByUsername("google@example.com"))
                .thenThrow(new UsernameNotFoundException("not found"));
        when(jwtUtil.validateToken("google-token", "google@example.com")).thenReturn(true);

        filter.doFilter(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("google@example.com", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void shouldNotAuthenticateWhenTokenExtractionFails() throws Exception {
        JwtAuthFilter filter = new JwtAuthFilter(jwtUtil, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer broken-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> { };

        when(jwtUtil.extractEmail("broken-token")).thenThrow(new RuntimeException("bad token"));

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
