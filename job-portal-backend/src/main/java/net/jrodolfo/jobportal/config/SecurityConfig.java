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
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static net.jrodolfo.jobportal.constant.Role.ADMIN;
import static net.jrodolfo.jobportal.constant.Role.APPLICANT;


@Configuration
public class SecurityConfig {

    @Value("${ALLOWED_ORIGINS:http://localhost:5173}")
    private String allowedOrigins;

    @Value("${FRONTEND_BASE_URL:http://localhost:5173}")
    private String frontendBaseUrl;

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

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/jobs").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/jobs/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/jobs/**").hasRole("ADMIN")
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
