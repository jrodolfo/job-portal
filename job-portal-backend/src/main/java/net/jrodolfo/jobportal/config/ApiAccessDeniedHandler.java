package net.jrodolfo.jobportal.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom implementation of {@link AccessDeniedHandler} for the API.
 * This handler is responsible for returning a structured JSON response when an authenticated user
 * attempts to access a protected API resource they do not have the necessary permissions for.
 */
@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * Handles an access-denied failure by setting the response status to 403 Forbidden
     * and returning a JSON message.
     *
     * @param request               the {@link HttpServletRequest} that resulted in an {@link AccessDeniedException}
     * @param response              the {@link HttpServletResponse} so that the user agent can be advised of the failure
     * @param accessDeniedException the {@link AccessDeniedException} that caused the invocation
     * @throws IOException      if an input or output exception occurs
     * @throws ServletException if a servlet exception occurs
     */
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"message\":\"You do not have permission to access this API resource.\"}");
    }
}
