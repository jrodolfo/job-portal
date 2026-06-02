package net.jrodolfo.jobportal.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom implementation of {@link AuthenticationEntryPoint} for the API.
 * This entry point is invoked when an unauthenticated user attempts to access a protected
 * API resource, ensuring that the response is returned in a standard JSON format.
 */
@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Starts an authentication scheme by setting the response status to 401 Unauthorized
     * and returning a JSON message.
     *
     * @param request       the {@link HttpServletRequest} that resulted in an {@link AuthenticationException}
     * @param response      the {@link HttpServletResponse} so that the user agent can begin authentication
     * @param authException the {@link AuthenticationException} that caused the invocation
     * @throws IOException      if an input or output exception occurs
     * @throws ServletException if a servlet exception occurs
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"message\":\"" + getMessage(authException) + "\"}");
    }

    private String getMessage(AuthenticationException authException) {
        if (isDisabledAccount(authException)) {
            return "Your account is disabled. Please contact an administrator.";
        }

        return "Invalid email or password.";
    }

    private boolean isDisabledAccount(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof DisabledException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
