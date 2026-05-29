package net.jrodolfo.jobportal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static net.jrodolfo.jobportal.constant.Role.ADMIN;
import static net.jrodolfo.jobportal.constant.Role.APPLICANT;


/**
 * Main security configuration class for the application.
 * Defines the security filter chain, authentication mechanisms (HTTP Basic, JWT, OAuth2),
 * CORS policy, and authorization rules for different API endpoints.
 */
@Configuration
public class SecurityConfig {

    /**
     * The origins allowed to access the API via CORS, typically the frontend URL.
     */
    @Value("${ALLOWED_ORIGINS:http://localhost:5173}")
    private String allowedOrigins;

    /**
     * The base URL of the frontend application, used for OAuth2 redirect flows.
     */
    @Value("${FRONTEND_BASE_URL:http://localhost:5173}")
    private String frontendBaseUrl;

    /**
     * Configures in-memory users for development and basic authentication.
     *
     * @return a {@link UserDetailsService} with predefined admin and applicant users
     */
    @Bean
    UserDetailsService userDetailsService() {
        UserDetails admin = User.withUsername("admin")
                .password("{noop}admin123")
                .roles(ADMIN.getValue())
                .build();
        UserDetails applicant = User.withUsername("user")
                .password("{noop}user123")
                .roles(APPLICANT.getValue())
                .build();
        return new InMemoryUserDetailsManager(admin, applicant);
    }

    /**
     * Configures the {@link SecurityFilterChain} which defines the security behavior for HTTP requests.
     * <p>
     * This includes:
     * <ul>
     *     <li>Disabling CSRF (as the API uses JWT/stateless auth for some parts)</li>
     *     <li>Defining authorization rules for endpoints based on roles</li>
     *     <li>Setting up custom exception handling for authentication and access denial</li>
     *     <li>Registering the {@link JwtAuthFilter} before the standard username/password filter</li>
     *     <li>Configuring HTTP Basic and OAuth2 Login with custom success/failure handlers</li>
     * </ul>
     *
     * @param http                        the {@link HttpSecurity} to configure
     * @param jwtAuthFilter               the custom JWT filter
     * @param apiAuthenticationEntryPoint the custom entry point for authentication failures
     * @param apiAccessDeniedHandler      the custom handler for access denial failures
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            JwtAuthFilter jwtAuthFilter,
                                            ApiAuthenticationEntryPoint apiAuthenticationEntryPoint,
                                            ApiAccessDeniedHandler apiAccessDeniedHandler) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/jobs").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/jobs/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/jobs/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/jobs/admin").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/jobs").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/jobs/**").permitAll()
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/applications/**").hasRole("APPLICANT")
                        .requestMatchers(HttpMethod.GET, "/api/applications/**").hasAnyRole("APPLICANT", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/applications/**").hasAnyRole("APPLICANT", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/applications/**").hasAnyRole("APPLICANT", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").authenticated()
                        .requestMatchers("/oauth/**", "/login/**", "/oauth2/**", "/api/oauth/**").permitAll() //Allow OAuth endpoints
                        .requestMatchers(HttpMethod.GET, "/api/auth/details").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                apiAuthenticationEntryPoint,
                                new AntPathRequestMatcher("/api/**")
                        )
                        .defaultAccessDeniedHandlerFor(
                                apiAccessDeniedHandler,
                                new AntPathRequestMatcher("/api/**")
                        )
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults())
                //.oauth2Login(Customizer.withDefaults()) //Enable Google OAuth login
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(userRequest -> {
                                    var delegate = new DefaultOAuth2UserService();
                                    var oauth2User = delegate.loadUser(userRequest);

                                    // Assign ROLE_APPLICANT to every OAuth user
                                    return new DefaultOAuth2User(
                                            List.of(new SimpleGrantedAuthority("ROLE_APPLICANT")),
                                            oauth2User.getAttributes(),
                                            "email"
                                    );
                                })
                        )
                        .successHandler((request, response, authentication) -> {
                            String redirectBase = frontendBaseUrl + "/oauthlogon";
                            if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
                                String idToken = oidcUser.getIdToken().getTokenValue();
                                String encodedToken = URLEncoder.encode(idToken, StandardCharsets.UTF_8);
                                response.sendRedirect(redirectBase + "?token=" + encodedToken);
                                return;
                            }
                            response.sendRedirect(redirectBase);
                        })
                        .failureHandler((request, response, exception) ->
                                response.sendRedirect(frontendBaseUrl + "/?oauth_error=true"))
                )
                .build();
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) settings.
     * Defines allowed origins, methods, and headers for requests coming from the frontend.
     *
     * @return the {@link UrlBasedCorsConfigurationSource} with the CORS configuration applied
     */
    @Bean
    UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
