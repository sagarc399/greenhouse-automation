package com.greenhouse.app.security;

import com.greenhouse.app.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Wrapper around {@link OAuth2User} that exposes the local {@link User} entity
 * and maps the application role to a Spring Security {@link GrantedAuthority}.
 *
 * <p>The authority is prefixed with {@code ROLE_} so that Spring Security
 * {@code hasRole()} expressions work without the prefix.</p>
 */
public class CustomOAuth2User implements OAuth2User {

    private final OAuth2User delegate;
    private final User user;

    /**
     * Constructs the wrapper.
     *
     * @param delegate  the underlying Spring Security OAuth2User
     * @param user      the corresponding local User entity
     */
    public CustomOAuth2User(OAuth2User delegate, User user) {
        this.delegate = delegate;
        this.user = user;
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    /**
     * Returns the single authority derived from the user's application role.
     *
     * @return a collection containing {@code ROLE_ADMIN} or {@code ROLE_OPERATOR}
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    /**
     * Returns the user's email as the principal name.
     *
     * @return the user's email address
     */
    @Override
    public String getName() {
        return user.getEmail();
    }

    /**
     * Returns the local User entity.
     *
     * @return the authenticated user
     */
    public User getUser() {
        return user;
    }
}
