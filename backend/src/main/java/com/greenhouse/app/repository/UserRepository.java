package com.greenhouse.app.repository;

import com.greenhouse.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link User} entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     *
     * @param email the email to search for
     * @return an Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a user by their OAuth2 provider and provider-specific subject ID.
     *
     * @param provider   the OAuth2 provider name (e.g., "google")
     * @param providerId the subject ID from the provider
     * @return an Optional containing the user if found
     */
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
}
