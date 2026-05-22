package com.greenhouse.app.config;

import com.greenhouse.app.security.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration for the Greenhouse Management System.
 *
 * <h2>Role-based access rules</h2>
 * <ul>
 *   <li><b>ADMIN</b>: full access to all endpoints.</li>
 *   <li><b>OPERATOR</b>: GET on all resources, POST on sensor-readings,
 *       PUT on alert status.</li>
 * </ul>
 *
 * <p>OAuth2 login is configured for Google. After successful authentication the
 * user is redirected to {@code /api/dashboard}.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService oAuth2UserService;

    /**
     * Constructs the configuration with the custom OAuth2 user service.
     *
     * @param oAuth2UserService service that persists authenticated users
     */
    public SecurityConfig(CustomOAuth2UserService oAuth2UserService) {
        this.oAuth2UserService = oAuth2UserService;
    }

    /**
     * Defines the security filter chain with CORS, CSRF, authorization rules,
     * and OAuth2 login.
     *
     * @param http the HttpSecurity builder
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if the configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Public: Swagger UI and API docs
                .requestMatchers(
                    "/swagger-ui.html", "/swagger-ui/**",
                    "/api-docs", "/api-docs/**",
                    "/v3/api-docs", "/v3/api-docs/**"
                ).permitAll()
                // Public: OAuth2 login
                .requestMatchers("/login/**", "/oauth2/**").permitAll()

                // ADMIN: write operations on most resources
                .requestMatchers(HttpMethod.POST, "/api/greenhouses/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/greenhouses/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/greenhouses/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/zones/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/zones/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/zones/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/sensors/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/sensors/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/sensors/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/actuators/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/actuators/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/actuators/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/automation-rules/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/automation-rules/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/automation-rules/**").hasRole("ADMIN")

                // OPERATOR: can POST sensor readings
                .requestMatchers(HttpMethod.POST, "/api/sensor-readings/**").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers(HttpMethod.DELETE, "/api/sensor-readings/**").hasRole("ADMIN")

                // OPERATOR: can update alert status
                .requestMatchers(HttpMethod.PUT, "/api/alerts/**").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers(HttpMethod.POST, "/api/alerts/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/alerts/**").hasRole("ADMIN")

                // All authenticated users: GET on any resource
                .requestMatchers(HttpMethod.GET, "/api/**").authenticated()

                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(ui -> ui.userService(oAuth2UserService))
                .defaultSuccessUrl("/api/dashboard", true)
            );

        return http.build();
    }

    /**
     * CORS configuration allowing the Vue frontend (localhost:5173) to call the API.
     *
     * @return the CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
