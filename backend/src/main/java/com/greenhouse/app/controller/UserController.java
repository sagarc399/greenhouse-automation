package com.greenhouse.app.controller;

import com.greenhouse.app.entity.User;
import com.greenhouse.app.repository.UserRepository;
import com.greenhouse.app.security.CustomOAuth2User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes the currently authenticated user's profile.
 *
 * <p>The Vue frontend calls {@code GET /api/user/me} on every page load to check
 * whether the browser's session cookie is still valid and to retrieve the user's
 * name, email, and role for display and role-based navigation.</p>
 *
 * <p>Spring Security protects this endpoint automatically; unauthenticated requests
 * receive HTTP 401 JSON from the custom {@code AuthenticationEntryPoint} configured
 * in {@link com.greenhouse.app.config.SecurityConfig}.</p>
 *
 * <h2>Principal type handling</h2>
 * <p>Google uses OIDC (it always sends an ID token when the {@code openid} scope is
 * requested). Depending on how the OIDC user service is wired, the principal stored
 * in the session can be one of three types:</p>
 * <ul>
 *   <li>{@link CustomOAuth2User} — our wrapper; the {@link User} entity is directly
 *       available via {@code getUser()}.</li>
 *   <li>{@link OidcUser} — Spring's {@code DefaultOidcUser}; name and email come
 *       from the OIDC ID-token claims, role is resolved from the database.</li>
 *   <li>{@link OAuth2User} — generic fallback; attributes map contains {@code name}
 *       and {@code email}, role is resolved from the database.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/user")
@Tag(name = "User", description = "Current authenticated user profile")
public class UserController {

    private final UserRepository userRepository;

    /**
     * Constructs the controller with the user repository used for role look-ups.
     *
     * @param userRepository JPA repository for resolving the persisted user role
     */
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns the profile of the currently authenticated user.
     *
     * <p>The method inspects the principal type at runtime rather than casting
     * blindly, so it never throws a {@link ClassCastException} regardless of which
     * Spring Security OAuth2 provider path was used during login.</p>
     *
     * <p>When the principal is not a {@link CustomOAuth2User} (i.e. OIDC path),
     * the role is resolved by looking up the user in the database by email.
     * If no matching record exists (first-time login before persistence completes),
     * the role field in the response is {@code null}.</p>
     *
     * @param authentication the active Spring Security authentication — injected
     *                       automatically; never {@code null} on this protected endpoint
     * @return HTTP 200 with a {@link UserMeResponse} containing name, email, and role
     */
    @GetMapping("/me")
    @Operation(
        summary = "Get currently authenticated user",
        description = "Returns the name, email, and role from the active Spring Security session. "
            + "Returns 401 if no valid session cookie is present."
    )
    @ApiResponse(responseCode = "200", description = "Authenticated user profile")
    @ApiResponse(responseCode = "401", description = "No active session — user must log in")
    public ResponseEntity<UserMeResponse> getCurrentUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        // ── Best path ──────────────────────────────────────────────────────────
        // CustomOAuth2UserService wrapped the principal — DB User is directly
        // available with all fields populated.
        if (principal instanceof CustomOAuth2User customUser) {
            User user = customUser.getUser();
            return ResponseEntity.ok(new UserMeResponse(
                    user.getName(),
                    user.getEmail(),
                    user.getRole() != null ? user.getRole().name() : null
            ));
        }

        // ── OIDC / OAuth2 fallback ─────────────────────────────────────────────
        // Google login with openid scope → Spring stores a DefaultOidcUser when
        // the OidcUserService is not overridden.  Extract claims from whichever
        // principal type was stored, then resolve the role from the database.
        String name;
        String email;

        if (principal instanceof OidcUser oidcUser) {
            // ID-token claims — these are always present for Google OIDC.
            name  = oidcUser.getFullName();
            email = oidcUser.getEmail();
        } else if (principal instanceof OAuth2User oauth2User) {
            // Generic OAuth2 attributes map.
            name  = oauth2User.getAttribute("name");
            email = oauth2User.getAttribute("email");
        } else {
            // Unknown type — use whatever Spring set as the principal name.
            name  = authentication.getName();
            email = null;
        }

        // Resolve the persisted role for this email address.
        // Returns null if the user has not been persisted yet (unlikely in
        // normal flow because CustomOAuth2UserService upserts on every login).
        String role = null;
        if (email != null) {
            role = userRepository.findByEmail(email)
                    .map(u -> u.getRole() != null ? u.getRole().name() : null)
                    .orElse(null);
        }

        return ResponseEntity.ok(new UserMeResponse(name, email, role));
    }

    /**
     * Immutable response payload returned by {@link #getCurrentUser}.
     *
     * @param name  display name from the Google OAuth2 / OIDC profile
     * @param email email address from the Google OAuth2 / OIDC profile
     * @param role  application role ({@code ADMIN} or {@code OPERATOR}),
     *              or {@code null} if the user has no persisted record yet
     */
    public record UserMeResponse(String name, String email, String role) {}
}
