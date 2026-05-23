package com.greenhouse.app.security;

import com.greenhouse.app.entity.User;
import com.greenhouse.app.repository.UserRepository;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 * Custom OIDC user service that persists Google-authenticated users on every login.
 *
 * <p>When Google issues an ID token with the {@code openid}, {@code profile}, and
 * {@code email} scopes, Spring Security uses {@link OidcUserService} — not the
 * {@link DefaultOAuth2UserService}-based {@link CustomOAuth2UserService} — as the
 * primary handler.  If the token already contains all required claims, Spring may
 * skip the userinfo endpoint entirely, meaning {@link CustomOAuth2UserService} is
 * never called as a delegate and the user is never upserted in the database.</p>
 *
 * <p>This service extends {@link OidcUserService} so it is wired directly into the
 * OIDC authentication path.  It delegates the actual OIDC user loading to the parent
 * implementation and then guarantees a DB upsert from the ID-token claims.</p>
 *
 * <h2>Role assignment</h2>
 * <p>First-time users receive {@code OPERATOR}.  An administrator must manually
 * promote users to {@code ADMIN} via the database.</p>
 */
@Service
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    /**
     * Constructs the service with the user repository.
     *
     * @param userRepository JPA repository for persisting authenticated users
     */
    public CustomOidcUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads the OIDC user from the provider and upserts the local user record.
     *
     * <p>The parent implementation handles token validation and ID-token parsing.
     * This override reads {@code email}, {@code name}, and {@code picture} from the
     * resolved {@link OidcUser} claims and persists or updates the matching
     * {@link User} entity.  The original {@link OidcUser} is returned unchanged so
     * that Spring Security stores it as the session principal — downstream code in
     * {@link com.greenhouse.app.controller.UserController} resolves the role via the
     * DB using the same email.</p>
     *
     * @param userRequest the OIDC user request containing the ID token and client info
     * @return the {@link OidcUser} produced by the parent service
     * @throws OAuth2AuthenticationException if the provider response is invalid
     */
    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String email      = oidcUser.getEmail();
        String name       = oidcUser.getFullName();
        String picture    = oidcUser.getAttribute("picture");
        String provider   = userRequest.getClientRegistration().getRegistrationId();
        String providerId = oidcUser.getSubject();

        if (email != null) {
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setEmail(email);
                        newUser.setProvider(provider);
                        newUser.setProviderId(providerId);
                        newUser.setRole(User.UserRole.OPERATOR);
                        return newUser;
                    });

            user.setName(name);
            user.setPictureUrl(picture);
            userRepository.save(user);
        }

        return oidcUser;
    }
}
