package com.greenhouse.app.config;

import com.greenhouse.app.security.CustomOAuth2UserService;
import com.greenhouse.app.security.CustomOidcUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.NullRequestCache;
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
 * user is redirected to the Vue frontend URL configured via
 * {@code app.frontend.base-url} in {@code application.properties}.</p>
 *
 * <p>Unauthenticated requests to {@code /api/**} receive HTTP 401 JSON instead of
 * the default Spring Security redirect to the OAuth2 login page, so that the Vue
 * frontend can distinguish "not logged in" from a real error response.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /** Custom OAuth2 user service for non-OIDC provider logins. */
    private final CustomOAuth2UserService oAuth2UserService;

    /** Custom OIDC user service for Google logins; persists users from ID-token claims. */
    private final CustomOidcUserService  oidcUserService;

    /**
     * Base URL of the Vue frontend (e.g. {@code http://localhost:5173}).
     * Injected from {@code app.frontend.base-url} in {@code application.properties}.
     */
    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    /**
     * Constructs the configuration with both custom user services.
     *
     * @param oAuth2UserService plain OAuth2 user service (non-OIDC providers)
     * @param oidcUserService   OIDC user service for Google (persists users from ID-token claims)
     */
    public SecurityConfig(CustomOAuth2UserService oAuth2UserService,
                          CustomOidcUserService   oidcUserService) {
        this.oAuth2UserService = oAuth2UserService;
        this.oidcUserService   = oidcUserService;
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
            // Disable Spring's saved-request cache so that a background XHR to
            // /api/user/me (or any other API call) is never stored as the
            // post-login redirect target.  Without this, if the session expires
            // while the Vue app is already open, Spring would save
            // "GET /api/user/me" and redirect the browser there after the next
            // successful OAuth2 login — showing raw JSON at localhost:8080.
            // alwaysUseDefaultTargetUrl(true) on the success handler is a second
            // layer of protection; NullRequestCache is the first.
            .requestCache(cache -> cache.requestCache(new NullRequestCache()))
            // Return HTTP 401 JSON for unauthenticated /api/** calls instead of
            // redirecting to the OAuth2 login page (which confuses Axios).
            // For all other paths, fall through to the default OAuth2 redirect.
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                    } else {
                        response.sendRedirect("/oauth2/authorization/google");
                    }
                })
            )
            .authorizeHttpRequests(auth -> auth
                // Public: Swagger UI and API docs
                .requestMatchers(
                    "/swagger-ui.html", "/swagger-ui/**",
                    "/api-docs", "/api-docs/**",
                    "/v3/api-docs", "/v3/api-docs/**"
                ).permitAll()
                // Public: OAuth2 login
                .requestMatchers("/login/**", "/oauth2/**").permitAll()

                // OPERATOR write exceptions — must come before the ADMIN-only catch-all rules
                .requestMatchers(HttpMethod.POST, "/api/sensor-readings/**").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers(HttpMethod.PUT,  "/api/alerts/**").hasAnyRole("ADMIN", "OPERATOR")

                // ADMIN: all mutating operations on any API resource
                .requestMatchers(HttpMethod.POST,   "/api/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH,  "/api/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")

                // ADMIN + OPERATOR: read access to all resources
                .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "OPERATOR")

                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(ui -> ui
                    // Non-OIDC OAuth2 providers (if any are added later).
                    .userService(oAuth2UserService)
                    // Google uses OIDC: wire our custom service so the user is
                    // always persisted from the ID-token claims before the
                    // session is created, regardless of whether the userinfo
                    // endpoint is called.
                    .oidcUserService(oidcUserService)
                )
                .successHandler(frontendRedirectSuccessHandler())
            )
            // After logout, invalidate the session, delete the cookie, and
            // redirect the browser (or return 200 for the XHR path) to the
            // Vue frontend login page.
            //
            // logoutUrl — explicit so Spring registers LogoutFilter for
            //   POST /logout; CSRF is disabled, so the frontend can POST via
            //   Axios without a CSRF token.
            // deleteCookies — removes the JSESSIONID from the browser's jar
            //   so a stale cookie can never replay an old session.
            // logoutSuccessUrl — absolute URL; the Vue frontend handles the
            //   router navigation after the POST completes.
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl(frontendBaseUrl + "/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }

    /**
     * Authentication success handler that redirects the browser to the Vue frontend
     * dashboard after a successful OAuth2 login.
     *
     * <p>Using {@link SimpleUrlAuthenticationSuccessHandler} with an absolute URL
     * (e.g. {@code http://localhost:5173/dashboard}) is required because
     * {@code defaultSuccessUrl} only supports paths relative to the backend server,
     * which would keep the user on port 8080 instead of returning to the frontend.</p>
     *
     * @return a handler that always redirects to {@code {frontendBaseUrl}/dashboard}
     */
    @Bean
    public SimpleUrlAuthenticationSuccessHandler frontendRedirectSuccessHandler() {
        SimpleUrlAuthenticationSuccessHandler handler = new SimpleUrlAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl(frontendBaseUrl + "/dashboard");
        handler.setAlwaysUseDefaultTargetUrl(true);
        return handler;
    }

    /**
     * CORS configuration allowing the Vue frontend ({@code localhost:5173}) to call
     * the backend API with session cookies.
     *
     * <p>{@code allowCredentials = true} is required so the browser sends the Spring
     * session cookie on cross-origin XHR/fetch calls ({@code axios withCredentials}).</p>
     *
     * <p>The configuration is registered on {@code /api/**} (REST endpoints) and
     * {@code /logout} so that the frontend's logout redirect is also permitted.</p>
     *
     * @return the CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        source.registerCorsConfiguration("/logout", config);
        return source;
    }
}
