package com.greenhouse.app.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

/**
 * Cookie-backed implementation of {@link AuthorizationRequestRepository}.
 *
 * <h2>Why this exists</h2>
 * <p>Spring Security's default implementation stores the OAuth2 authorization
 * request (including the {@code state} CSRF token) in the HTTP session.  In
 * production, the frontend lives on Vercel and the backend lives on Railway —
 * two different domains.  When Google redirects back to the Railway callback
 * URL, the browser may not include the {@code JSESSIONID} cookie because of
 * cross-origin cookie restrictions, causing Spring to lose the stored request
 * and throw {@code authorization_request_not_found}.</p>
 *
 * <h2>How this fixes it</h2>
 * <p>Instead of the session, the authorization request is serialized into a
 * short-lived cookie ({@code oauth2_auth_request}, 3 minutes) set on the
 * Railway domain.  The Google callback is always a top-level browser
 * navigation back to that same Railway domain, so the cookie is reliably
 * present regardless of how the frontend origin relates to the backend.</p>
 *
 * <p>The cookie is deleted as part of {@link #removeAuthorizationRequest} so
 * it does not linger after the OAuth2 flow completes.</p>
 */
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    /** Name of the cookie that carries the serialized authorization request. */
    public static final String OAUTH2_AUTH_REQUEST_COOKIE = "oauth2_auth_request";

    /**
     * Max-age of the cookie in seconds.  The Google redirect round-trip should
     * complete in well under 3 minutes; a longer value would only extend the
     * window during which a stolen cookie could be replayed.
     */
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return CookieUtils.getCookie(request, OAUTH2_AUTH_REQUEST_COOKIE)
                .map(cookie -> CookieUtils.deserialize(cookie, OAuth2AuthorizationRequest.class))
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        if (authorizationRequest == null) {
            CookieUtils.deleteCookie(request, response, OAUTH2_AUTH_REQUEST_COOKIE);
            return;
        }
        CookieUtils.addCookie(response, OAUTH2_AUTH_REQUEST_COOKIE,
                CookieUtils.serialize(authorizationRequest), COOKIE_EXPIRE_SECONDS);
    }

    /**
     * Loads and immediately removes the authorization request so the cookie
     * does not persist beyond the end of the OAuth2 flow.
     */
    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                  HttpServletResponse response) {
        OAuth2AuthorizationRequest authRequest = loadAuthorizationRequest(request);
        if (authRequest != null) {
            CookieUtils.deleteCookie(request, response, OAUTH2_AUTH_REQUEST_COOKIE);
        }
        return authRequest;
    }
}
