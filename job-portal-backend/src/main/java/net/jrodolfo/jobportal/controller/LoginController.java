package net.jrodolfo.jobportal.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.jrodolfo.jobportal.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for handling authentication and user identity details.
 * <p>
 * Provides endpoints for user login, JWT token generation, and retrieving authenticated user information.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Auth", description = "Authentication and user identity endpoints")
public class LoginController {

    /**
     * Utility for generating and validating JSON Web Tokens (JWT).
     */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Authenticates a user and generates a JWT token.
     * <p>
     * This endpoint relies on Spring Security's basic authentication to verify the user's credentials
     * provided in the request before generating the token.
     *
     * @param principal the authenticated principal representing the logged-in user
     * @return a {@link Map} containing the generated "token"
     */
    @PostMapping("/login")
    @Operation(summary = "Login and get JWT token", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "JWT token returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    public Map<String, String> login(Principal principal) {
        String email = principal.getName(); // fetched from Spring Security
        System.out.println("email: " + email);
        // Generate token
        String token = jwtUtil.generateToken(email);
        System.out.println("token: " + token);

        // Return as JSON
        Map<String, String> response = new HashMap<>();
        response.put("token", token);

        return response;
    }

    /**
     * Retrieves the details of the currently authenticated user.
     * <p>
     * Returns the username and assigned roles (authorities) from the security context.
     *
     * @param principal the authenticated principal
     * @return a {@link Map} containing user details such as "username" and "roles", or an "error" message if not authenticated
     */
    @GetMapping("/details")
    @Operation(summary = "Get authenticated user details", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "User details returned", content = @Content(schema = @Schema(implementation = Map.class)))
    public Map<String, Object> getUserDetails(Principal principal) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            response.put("username", userDetails.getUsername());
            response.put("roles", userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
        } else {
            response.put("error", "User NOT authenticated");
            System.out.println("\n\n\t\tUser not authenticated\n\n\n");
        }
        return response;
    }
}
