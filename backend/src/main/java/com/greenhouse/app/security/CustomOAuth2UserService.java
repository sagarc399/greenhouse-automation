package com.greenhouse.app.security;

import com.greenhouse.app.entity.User;
import com.greenhouse.app.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * Custom OAuth2 user service that persists Google-authenticated users in the
 * local database on first login and returns a {@link CustomOAuth2User} wrapping
 * the Spring Security principal.
 *
 * <p>On each login the display name and profile picture are refreshed from the
 * OAuth2 provider to stay current.</p>
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    /**
     * Constructs the service with the user repository.
     *
     * @param userRepository JPA repository for persisting authenticated users
     */
    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads the user from the OAuth2 provider and upserts them in the database.
     *
     * @param userRequest the OAuth2 user request containing provider details
     * @return a {@link CustomOAuth2User} enriched with local application roles
     * @throws OAuth2AuthenticationException if the provider response is invalid
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

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

        return new CustomOAuth2User(oAuth2User, user);
    }
}
