package com.atena.events.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

/**
 * Intercepts the OAuth2 authorization redirect to capture an optional
 * upgradeGuestId query parameter and store it in the HTTP session.
 * The OAuthSuccessHandler reads it back after the OAuth flow completes.
 */
@Component
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    static final String SESSION_KEY = "UPGRADE_GUEST_ID";

    private final DefaultOAuth2AuthorizationRequestResolver delegate;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository repo) {
        this.delegate = new DefaultOAuth2AuthorizationRequestResolver(
            repo, "/oauth2/authorization"
        );
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest req = delegate.resolve(request);
        captureGuestId(request, req);
        return req;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest req = delegate.resolve(request, clientRegistrationId);
        captureGuestId(request, req);
        return req;
    }

    private void captureGuestId(HttpServletRequest request, OAuth2AuthorizationRequest req) {
        if (req == null) return;
        String guestId = request.getParameter("upgradeGuestId");
        if (guestId != null && !guestId.isBlank()) {
            HttpSession session = request.getSession(true);
            session.setAttribute(SESSION_KEY, guestId);
        }
    }
}
