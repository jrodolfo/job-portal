package net.jrodolfo.jobportal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for Google OAuth2 integration and token management.
 * <p>
 * Provides endpoints for retrieving authenticated OAuth user information, obtaining ID tokens,
 * and exchanging authorization codes for tokens using Google's OAuth2 services.
 */
@RestController
@RequestMapping("/api/oauth")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "OAuth", description = "Google OAuth helper endpoints")
public class OAuthController {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    /**
     * Retrieves the attributes of the currently authenticated OAuth2 user.
     *
     * @param authentication the OAuth2 authentication token containing user details
     * @return a {@link Map} of user attributes (e.g., email, name, picture)
     * @throws IllegalStateException if the user is not authenticated
     */
    @GetMapping("/user")
    @Operation(summary = "Get OAuth authenticated user attributes", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponse(responseCode = "200", description = "OAuth user attributes returned")
    public Map<String, Object> getUserInfo(OAuth2AuthenticationToken authentication) {
        if (authentication == null) {
            throw new IllegalStateException("User is not authenticated");
        }

        OidcUser user = (OidcUser) authentication.getPrincipal();
        return user.getAttributes();
    }

    /**
     * Retrieves the Google ID token for the currently authenticated OAuth2 user.
     *
     * @param authentication the OAuth2 authentication token
     * @return the raw ID token value as a {@link String}
     */
    @GetMapping("/token")
    @Operation(summary = "Get Google ID token for authenticated OAuth user", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponse(responseCode = "200", description = "Google token returned")
    public String getAccessToken(OAuth2AuthenticationToken authentication) {
        OidcUser user = (OidcUser) authentication.getPrincipal();
        return user.getIdToken().getTokenValue(); //Access token from google
    }

    /**
     * Exchanges a Google OAuth2 authorization code for an ID token.
     * <p>
     * Communicates directly with Google's token endpoint ({@code https://oauth2.googleapis.com/token})
     * using a {@link RestTemplate} to perform the exchange.
     *
     * @param body a {@link Map} containing the "code" received from Google
     * @return a {@link Map} containing the exchanged "token" (ID token)
     */
    @PostMapping("/exchange-token")
    @Operation(summary = "Exchange OAuth authorization code for token")
    @ApiResponse(responseCode = "200", description = "Exchanged token returned")
    public Map<String, String> exchangeToken(@RequestBody Map<String, String> body) {
        String code = body.get("code");

        String redirectUri = "http://localhost:5173/oauthlogon";

        String tokenUrl = "https://oauth2.googleapis.com/token";

        Map<String, String> params = new HashMap<>();
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("code", code);
        params.put("redirect_uri", redirectUri);
        params.put("grant_type", "authorization_code");

        RestTemplate restTemplate = new RestTemplate();
        ParameterizedTypeReference<Map<String, Object>> typeRef =
                new ParameterizedTypeReference<Map<String, Object>>() {
                };

        ResponseEntity<Map<String, Object>> response
                = restTemplate.exchange(tokenUrl, HttpMethod.POST, new HttpEntity<>(params), typeRef);

        String idToken = (String) response.getBody().get("id_token");

        Map<String, String> result = new HashMap<>();
        result.put("token", idToken);
        return result;
    }

    /**
     * Retrieves user details from Google using a provided ID token.
     * <p>
     * Validates the token by calling Google's tokeninfo endpoint.
     *
     * @param token the Bearer token (ID token) provided in the {@code Authorization} header
     * @return a {@link Map} of user details as returned by Google's verification service
     */
    @GetMapping("/user-details")
    @Operation(summary = "Get user details from Google token", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Google user details returned")
    public Map<String, Object> getUserInfo(@RequestHeader("Authorization") String token) {
        String idToken = token.replace("Bearer ", "");

        //Verify the id token with google
        String userInfoEndpoint = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                userInfoEndpoint,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                }
        );

        Map<String, Object> body = response.getBody();
        return body;
    }
}
